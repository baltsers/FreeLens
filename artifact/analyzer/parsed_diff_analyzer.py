import json
import argparse
from typing import Dict, List, Set, Any
from copy import deepcopy
from collections import defaultdict



class DiffAnalyzer:
    def __init__(self, input_file: str):
        with open(input_file, 'r') as f:
            self.diff_data = json.load(f)
    def is_obfuscated(self, method_signature: str) -> bool:
        """Determines if a method signature is considered obfuscated."""
        return method_signature.count(' ') < 2


    def extract_method_name_from_method_signature(self, method_signature):
    # "<com.chating.messages.feature.locationFeature.LocationTransparentActivity: void setBinding(com.example.mylibrary.databinding.ActivityTransparentBinding)>"
    # -> setBinding
         return method_signature.split(' ')[2].split('(')[0]

    def extract_class_from_method_signature(self, method_signature):
    # "<com.chating.messages.feature.locationFeature.LocationTransparentActivity: void setBinding(com.example.mylibrary.databinding.ActivityTransparentBinding)>"
    # -> com.chating.messages.feature.locationFeature.LocationTransparentActivity
        return method_signature.split(':')[0].split('<')[1]

    def remove_constructors(self) -> Dict:
        """Remove all methods containing <init> or <clinit>"""
        result = deepcopy(self.diff_data)

        for activity in result:
            for entry_method in result[activity]:
                for change_type in ['added', 'removed']:
                    patterns = result[activity][entry_method][change_type]
                    for pattern in patterns:
                        # Filter SDK methods
                        pattern['sdk_methods'] = [
                            method for method in pattern['sdk_methods']
                            if '<init' not in method and '<clinit' not in method
                        ]
                        # Filter intermediate patterns
                        pattern['intermediate_pattern'] = [
                            method for method in pattern['intermediate_pattern']
                            if '<init' not in method and '<clinit' not in method
                        ]
                        # Update depth to reflect new intermediate pattern length
                        pattern['depth'] = len(pattern['intermediate_pattern'])

        return result

    def create_method_mapping(self, diff_data: Dict) -> Dict[str, int]:
        """Create a mapping from method names to numbers"""
        """Create a mapping from method names to numbers, replacing obfuscated names with a placeholder."""
        all_methods = set()
        obfuscated_placeholder = "<obfuscated_class: * obfuscated_method(*)>"

        # Collect all methods
        for activity in diff_data:
            for entry_method in diff_data[activity]:
                all_methods.add(entry_method)
                for change_type in ['added', 'removed']:
                    patterns = diff_data[activity][entry_method][change_type]
                    for pattern in patterns:
                        # Add SDK methods and intermediate methods from pattern
                        all_methods.update(pattern['sdk_methods'])
                        all_methods.update(pattern['intermediate_pattern'])

        # Filter methods and replace obfuscated signatures
        filtered_all_methods = set()
        for method in all_methods:
            if method.count(' ') < 2:  # Check if there are less than two spaces
                filtered_all_methods.add(obfuscated_placeholder)
            else:
                filtered_all_methods.add(method)

        # Create mapping from filtered methods
        return {method: i for i, method in enumerate(sorted(filtered_all_methods))}

    def create_adjacency_lists(self, use_numbers: bool = True) -> Dict:
        """Create adjacency lists for each activity's entry methods, handling obfuscated methods."""
        clean_diff = self.remove_constructors()
        method_mapping = self.create_method_mapping(clean_diff) if use_numbers else None
        obfuscated_placeholder = "<obfuscated_class: * obfuscated_method(*)>"

        result = {}

        for activity in clean_diff:
            result[activity] = {}
            for entry_method in clean_diff[activity]:
                added_adj_lists = []
                removed_adj_lists = []

                for change_type in ['added', 'removed']:
                    patterns = clean_diff[activity][entry_method][change_type]
                    for pattern in patterns:
                        # Handle obfuscation in paths
                        base_path = [entry_method] + pattern['intermediate_pattern']
                        if use_numbers:
                            base_path = [method_mapping.get(m, method_mapping.get(obfuscated_placeholder)) for m in base_path]

                        # Handle obfuscation in SDK methods
                        sdk_targets = pattern['sdk_methods']
                        if use_numbers:
                            sdk_targets = [method_mapping.get(m, method_mapping.get(obfuscated_placeholder)) for m in sdk_targets]

                        if change_type == 'added':
                            added_adj_lists.append({
                                "base_path": base_path,
                                "sdk_targets": sdk_targets
                            })
                        elif change_type == 'removed':
                            removed_adj_lists.append({
                                "base_path": base_path,
                                "sdk_targets": sdk_targets
                            })

                result[activity][entry_method] = {
                    'added': added_adj_lists,
                    'removed': removed_adj_lists
                }

        if use_numbers:
            # Add method mapping to result for reference
            result['method_mapping'] = {str(v): k for k, v in method_mapping.items()}

        return result

    def print_diff_tree(self, diff_result: Dict, debug: bool = False):
        """Print the diff result as a formatted tree"""
        method_mapping = {int(k): v for k, v in diff_result.get('method_mapping', {}).items()}

        # Organize methods by class
        class_to_methods = defaultdict(list)
        for node_id, method in method_mapping.items():
            class_name = self.extract_class_from_method_signature(method)
            class_to_methods[class_name].append((node_id, method))

        # Print method mapping organized by class
        print("[ID to Method Mapping]:")
        for class_name, methods in sorted(class_to_methods.items()):
            methods.sort()  # Sort methods by ID for consistent ordering
            print(f"{class_name}:")
            for node_id, method in methods:
                method_short_name = self.extract_method_name_from_method_signature(method)
                print(f"    {node_id}: {method_short_name}")
        print()  # Empty line after mapping

        print("[Changed Paths]:")
        for activity in diff_result:
            if activity == 'method_mapping':
                continue

            print()
            print(f"Activity: {activity}")
            for entry_method in diff_result[activity]:
                entry_method_id = method_mapping.get(entry_method, entry_method)
                entry_method_short_name = self.extract_method_name_from_method_signature(entry_method)
                print(f"- Entry Method: {entry_method_id}")

                # Process removed paths first
                removed_paths = diff_result[activity][entry_method].get('removed', [])
                if removed_paths:
                    print("- Removed Paths:")
                    self._print_path_tree(removed_paths, method_mapping, debug)

                # Process added paths
                added_paths = diff_result[activity][entry_method].get('added', [])
                if added_paths:
                    print("- Added Paths:")
                    self._print_path_tree(added_paths, method_mapping, debug)

    def _print_path_tree(self, paths: List[Dict], method_mapping: Dict, debug: bool = False):
        """Print paths as a tree structure"""
        if not paths:
            return

        # First convert numbered paths to a tree structure
        tree = defaultdict(list)
        for path in paths:
            base = tuple(path['base_path'])
            tree[base].extend(path['sdk_targets'])  # Added this line to populate the tree

        # Print tree
        printed_prefixes = set()
        for base_path in sorted(tree.keys()):
            current_prefix = []
            for i, node in enumerate(base_path):
                current_prefix.append(node)
                prefix_tuple = tuple(current_prefix)
                if prefix_tuple not in printed_prefixes:
                    indent = "│ " * (i - 1) + ("├─" if i > 0 else "")
                    if i == len(base_path) - 1:
                        indent = "│ " * (i - 1) + ("└─" if i > 0 else "")
                    node_str = method_mapping.get(node, str(node)) if debug else str(node)
                    print(f"{indent}{node_str}")
                    printed_prefixes.add(prefix_tuple)

            targets = sorted(tree[base_path])
            if targets:  # Only print SDK targets if they exist
                indent = "│ " * (len(base_path) - 1) + "  └─"
                sdk_str = ", ".join(method_mapping.get(t, str(t)) if debug else str(t) for t in targets)
                print(f"{indent}{sdk_str}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--input-file', help='Input JSON file with parsed diff')
    parser.add_argument('--remove-constructor', action='store_true',
                        help='Remove constructor methods from diff')
    parser.add_argument('--adjacency-list', action='store_true',
                        help='Convert to adjacency lists with numbered nodes')
    parser.add_argument('--debug-adjacency-list', action='store_true',
                        help='Convert to adjacency lists with method names')
    parser.add_argument('--print-tree', action='store_true',
                        help='Print result as numbered tree')
    parser.add_argument('--print-tree-debug', action='store_true',
                        help='Print result as tree with method names')
    parser.add_argument('--output', '-o', help='Output file')

    args = parser.parse_args()
    analyzer = DiffAnalyzer(args.input_file)

    global package_name
    package_name = args.input_file.split('/')[-1].split('_')[0]
    print(f'App package name: {package_name}\n\n')

    if args.remove_constructor:
        result = analyzer.remove_constructors()
    elif args.adjacency_list:
        result = analyzer.create_adjacency_lists(use_numbers=True)
    elif args.debug_adjacency_list:
        result = analyzer.create_adjacency_lists(use_numbers=False)
    else:
        print("No analysis option specified")
        return

    if args.print_tree or args.print_tree_debug:
        analyzer.print_diff_tree(result, debug=args.print_tree_debug)
    elif args.output:
        with open(args.output, 'w') as f:
            json.dump(result, f, indent=2)
    else:
        print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
