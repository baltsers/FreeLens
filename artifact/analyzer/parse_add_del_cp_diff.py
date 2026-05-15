# For added and removed CPs, we do not use CPs, method signatures already enough
import sys
from collections import defaultdict
from typing import Dict, List, Tuple, Set
import json
import argparse


def extract_class_from_method_signature(method_signature):
    # "<com.chating.messages.feature.locationFeature.LocationTransparentActivity: void setBinding(com.example.mylibrary.databinding.ActivityTransparentBinding)>"
    # -> com.chating.messages.feature.locationFeature.LocationTransparentActivity
    return method_signature.split(':')[0].split('<')[1]

def extract_params_from_method_signature(method_signature):
    # "<com.chating.messages.feature.locationFeature.LocationTransparentActivity: void setBinding(com.example.mylibrary.databinding.ActivityTransparentBinding)>"
    # "-> com.example.mylibrary.databinding.ActivityTransparentBinding
    return  method_signature.split('(')[1].split(')')[0]

def extract_method_name_from_method_signature(method_signature):
    # "<com.chating.messages.feature.locationFeature.LocationTransparentActivity: void setBinding(com.example.mylibrary.databinding.ActivityTransparentBinding)>"
    # -> setBinding
    return method_signature.split(' ')[2].split('(')[0]

def extract_return_type_from_method_signature(method_signature):
    # "<com.chating.messages.feature.locationFeature.LocationTransparentActivity: void setBinding(com.example.mylibrary.databinding.ActivityTransparentBinding)>"
    # -> void
    return method_signature.split(' ')[1]

class MethodSignature:
    def __init__(self, signature: str):
        self.signature = signature
        self.class_name = extract_class_from_method_signature(signature)
        self.method_name = extract_method_name_from_method_signature(signature)

        self.return_type = extract_return_type_from_method_signature(signature)
        self.params = extract_params_from_method_signature(signature)

class ClassSignature:
    def __init__(self):
        self.class_name_length = 0
        self.method_count = 0
        self.symbolized_methods = []  # List of (return_type, params, symbolized_name)
        self.original_methods = []    # Keep track of original methods

def process_methods(added_methods: List[str], removed_methods: List[str]) -> Dict:
    # 1. Remove constructors and invalid methods first
    added = [m for m in added_methods if not is_invalid_method(m)]
    removed = [m for m in removed_methods if not is_invalid_method(m)]

    # 2. Group by class
    added_by_class = group_by_class(added)
    removed_by_class = group_by_class(removed)

    # Process each class's methods
    added_class_sigs = process_classes(added_by_class)
    removed_class_sigs = process_classes(removed_by_class)

    # Find and remove matched classes
    matched_classes = find_matched_classes(added_class_sigs, removed_class_sigs)

    # Prepare result using the new format
    result = {
        "ADDED_CLASSES": {},
        "REMOVED_CLASSES": {}
    }

    # Add remaining (unmatched) symbolized methods to result
    for class_name, class_info in added_class_sigs.items():
        if class_name not in matched_classes:
            # Extract method names only, discarding full signatures for the result
            method_names = [extract_method_name_from_method_signature(m) for m in class_info["methods"]]
            method_names_unique = set(method_names)
            result["ADDED_CLASSES"][class_name] = list(method_names_unique)

    for class_name, class_info in removed_class_sigs.items():
        if class_name not in matched_classes:
            # Extract method names only, discarding full signatures for the result
            method_names = [extract_method_name_from_method_signature(m) for m in class_info["methods"]]
            method_names_unique = set(method_names)
            result["REMOVED_CLASSES"][class_name] = list(method_names_unique)

    return result


def process_classes(classes_methods: Dict[str, List[str]]) -> Dict[str, Dict]:
    result = {}
    for class_name, methods in classes_methods.items():
        # Filter out invalid methods (e.g., constructors, callbacks)
        filtered_methods = [method for method in methods if not is_invalid_method(method)]

        # Group methods by signature length
        length_groups = defaultdict(list)
        for method in filtered_methods:
            length_groups[len(method)].append(method)

        # Store both symbolized and original methods
        final_methods = []

        # Process each length group
        for methods_same_length in length_groups.values():
            # Group by return type and parameters
            sig_groups = defaultdict(list)
            for method in methods_same_length:
                key = (extract_return_type_from_method_signature(method),
                       extract_params_from_method_signature(method))
                sig_groups[key].append(method)

            # Process each signature group
            for methods_group in sig_groups.values():
                # Only symbolize if there are multiple methods in the group
                if len(methods_group) > 1:
                    # If the method name length is ≤ 3, consider it obfuscated and symbolize
                    if len(extract_method_name_from_method_signature(methods_group[0])) <= 3:
                        symbolized_method = symbolize_methods(methods_group)
                        final_methods.append(symbolized_method)
                        #print("added symbolized", symbolized_method)
                    else:
                        # For non-obfuscated methods, add as is
                        final_methods.extend(methods_group)
                        #print("added non-obfuscated", methods_group)
                else:
                    # Single method in group: add directly without symbolizing
                    final_methods.extend(methods_group)
                    #print("added single method", methods_group)

        result[class_name] = {
            "class_name_length": len(class_name),
            "method_count": len(final_methods),
            "methods": final_methods,  # Keep the symbolized/verified methods for verification
            "methods_sig_length": sum(len(m) for m in final_methods)
        }

    return result


def find_matched_classes(added_sigs: Dict[str, Dict], removed_sigs: Dict[str, Dict]) -> Set[str]:
    matched = set()

    # Group by signature
    sig_to_classes = defaultdict(list)
    for class_name, sig in added_sigs.items():
        methods = sorted(sig["methods"])  # Use actual methods for comparison
        key = (sig["class_name_length"], sig["method_count"], tuple(methods))
        sig_to_classes[key].append(("added", class_name))

    for class_name, sig in removed_sigs.items():
        methods = sorted(sig["methods"])  # Use actual methods for comparison
        key = (sig["class_name_length"], sig["method_count"], tuple(methods))
        sig_to_classes[key].append(("removed", class_name))

    # Find matches
    for classes in sig_to_classes.values():
        added = [c[1] for c in classes if c[0] == "added"]
        removed = [c[1] for c in classes if c[0] == "removed"]

        # Match classes with same signature
        for a, r in zip(added, removed):
            matched.add(a)
            matched.add(r)

    return matched





def symbolize_methods(methods: List[str]) -> str:
    """Compare method signatures character by character and replace differences with '*'"""
    # if not methods:
    #     return ""
    #
    # # Start with the first method as the base
    # base = list(methods[0])
    #
    # # Iterate through each method in the list
    # for method in methods[1:]:
    #     # Compare each character with the base method
    #     for i in range(len(base)):
    #         if base[i] != method[i]:
    #             base[i] = '*'  # Replace with '*' if characters differ
    # # Join the list back into a string
    # return ''.join(base)
    return  methods[0]





def group_by_class(methods: List[str]) -> Dict[str, List[str]]:
    """
    Group methods by their class name

    Example:
    Input: [
        "<com.example.A: void m1()>",
        "<com.example.A: void m2()>",
        "<com.example.B: void m3()>"
    ]
    Output: {
        "com.example.A": ["<com.example.A: void m1()>", "<com.example.A: void m2()>"],
        "com.example.B": ["<com.example.B: void m3()>"]
    }
    """
    grouped = defaultdict(list)
    for method in methods:
        class_name = extract_class_from_method_signature(method)
        grouped[class_name].append(method)
    return dict(grouped)



def is_invalid_method(method_sig: str) -> bool:
    """
    Check if a method name is an Android callback that cannot be obfuscated
    """
    callbacks = {
        'onCreate', 'onStart', 'onResume', 'onPause', 'onStop', 'onDestroy',
        'onRestart', 'onSaveInstanceState', 'onRestoreInstanceState',
        'onCreateView', 'onActivityCreated', 'onViewCreated', 'onDestroyView',
        'onAttach', 'onDetach', 'onNewIntent', 'onActivityResult',
        'onRequestPermissionsResult', 'onConfigurationChanged', 'onBackPressed',
        'onWindowFocusChanged', 'onKeyDown', 'onCreateOptionsMenu', 'onOptionsItemSelected'
    }

    constructor = {
        '<init', '<clinit'
    }

    lambda_methods = {
        'lambda', 'Lambda'
    }

    for invalid_method in callbacks.union(constructor).union(lambda_methods):
        if invalid_method in method_sig:
            return True

    return False
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--input-file', help='Input JSON file with added/deleted methods')
    parser.add_argument('--output', '-o', help='Output file')

    args = parser.parse_args()

    if not args.input_file:
        data = sys.stdin.read()
        data = json.loads(data)
    else:
        with open(args.input_file, 'r') as f:
            data = json.load(f)

    # Process methods
    result = process_methods(
        added_methods=data.get("addedMethods", []),
        removed_methods=data.get("removedMethods", [])
    )

    # Output result
    if args.output:
        with open(args.output, 'w') as f:
            json.dump(result, f, indent=2)
    else:
        print(json.dumps(result, indent=2))

if __name__ == '__main__':
    main()