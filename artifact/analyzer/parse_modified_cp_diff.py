from typing import List, Dict, Set, Any
from dataclasses import dataclass
import json
from collections import defaultdict
import argparse

@dataclass
class CallPath:
    entryMethod: str
    sdkMethod: str
    depth: int
    intermediateMethods: List[str]

    def get_intermediate_length(self) -> int:
        return sum(len(method) for method in self.intermediateMethods)

    def to_dict(self) -> Dict:
        return {
            "entryMethod": self.entryMethod,
            "sdkMethod": self.sdkMethod,
            "depth": self.depth,
            "intermediateMethods": self.intermediateMethods
        }



@dataclass
class PatternGroup:
    depth: int
    intermediate_pattern: List[str]
    original_paths: List[CallPath]
    sdk_methods: Set[str]  # Changed to Set to store unique SDK methods

    def to_dict(self) -> Dict:
        return {
            "depth": self.depth,
            "intermediate_pattern": self.intermediate_pattern,
            "sdk_methods": list(self.sdk_methods),  # Convert set to list for JSON serialization
            #"original_paths": [p.to_dict() for p in self.original_paths]
        }
class CallPathPatternReducer:
    def __init__(self, json_data: Dict, third_party_sdk_initials: List[str]):
        self.activities_data = json_data["modifiedActivities"]
        self.third_party_sdk_initials = third_party_sdk_initials

    def find_common_pattern(self, methods: List[str]) -> List[str]:
        if not methods or not methods[0]:
            return []

        patterns = []
        for method_pos in range(len(methods[0])):
            base_method = methods[0][method_pos]
            pattern_chars = list(base_method)

            # Compare each character with other methods
            for i in range(len(pattern_chars)):
                for other_methods in methods[1:]:
                    if (i >= len(other_methods[method_pos]) or
                            other_methods[method_pos][i] != pattern_chars[i]):
                        pattern_chars[i] = '*'
                        break

            patterns.append(''.join(pattern_chars))

        return patterns

    def group_paths_by_depth_and_length(self, paths: List[CallPath]) -> Dict[int, Dict[int, List[CallPath]]]:
        # First group by depth
        depth_groups = defaultdict(list)
        for path in paths:
            depth_groups[path.depth].append(path)

        # Then for each depth group, group by intermediate length
        result = {}
        for depth, depth_paths in depth_groups.items():
            length_groups = defaultdict(list)
            for path in depth_paths:
                length_groups[path.get_intermediate_length()].append(path)
            result[depth] = dict(length_groups)

        return result

    def reduce_paths_to_patterns(self, call_paths: List[CallPath]) -> List[PatternGroup]:
        # Group paths by depth and intermediate length
        grouped_paths = self.group_paths_by_depth_and_length(call_paths)

        patterns = []
        for depth, length_groups in grouped_paths.items():
            for _, paths in length_groups.items():
                if paths:
                    # Get all intermediate methods lists
                    all_intermediates = [path.intermediateMethods for path in paths]
                    # Find common pattern
                    common_pattern = self.find_common_pattern(all_intermediates)
                    # Collect all unique SDK methods
                    sdk_methods = {path.sdkMethod for path in paths}
                    patterns.append(PatternGroup(
                        depth=depth,
                        intermediate_pattern=common_pattern,
                        original_paths=paths,
                        sdk_methods=sdk_methods
                    ))

        return patterns

    def process_activity(self, activity_data: Dict) -> Dict[str, Dict[str, List[PatternGroup]]]:
        result = {}

        for method_change in activity_data.get("modifiedMethodCallPaths", []):
            entry_method = method_change["entryMethod"]

            is_entry_method_third_party = False
            # Skip third-party SDKs as entry methods
            for sdk_initial in self.third_party_sdk_initials:
                if sdk_initial in entry_method:
                    is_entry_method_third_party = True
                    break

            if is_entry_method_third_party:
                continue

            change_report = method_change["changeReport"]

            if entry_method not in result:
                result[entry_method] = {"added": [], "removed": []}

            # Process added paths
            if change_report.get("addedPaths"):
                added_paths = [CallPath(**p) for p in change_report["addedPaths"]]
                result[entry_method]["added"] = self.reduce_paths_to_patterns(added_paths)

            # Process removed paths
            if change_report.get("removedPaths"):
                removed_paths = [CallPath(**p) for p in change_report["removedPaths"]]
                result[entry_method]["removed"] = self.reduce_paths_to_patterns(removed_paths)

        return result

    def to_json(self) -> Dict:
        patterns = self.process_all_activities()

        # Convert to JSON-serializable format
        result = {}
        for activity_name, method_patterns in patterns.items():
            if self.is_3rd_party_sdk(activity_name):
                continue
            result[activity_name] = {}
            for entry_method, changes in method_patterns.items():
                result[activity_name][entry_method] = {
                    "added": [pattern_group.to_dict() for pattern_group in changes["added"]],
                    "removed": [pattern_group.to_dict() for pattern_group in changes["removed"]]
                }

        return result

    def is_3rd_party_sdk(self, class_or_method: str) -> bool:
        for sdk_initial in self.third_party_sdk_initials:
            if sdk_initial in class_or_method:
                return True
        return False

    def process_all_activities(self) -> Dict[str, Dict[str, List[PatternGroup]]]:
        result = {}

        for activity_name, activity_data in self.activities_data.items():
            is_activity_third_party = False
            # Skip third-party SDKs
            if self.is_3rd_party_sdk(activity_name):
                continue

            if activity_data.get("modified"):
                result[activity_name] = self.process_activity(activity_data)

        return result

def printReducedDiff(result):
    print(json.dumps())

def main():
    # Load JSON data
    arg_parser = argparse.ArgumentParser()
    arg_parser.add_argument('--input-file', type=str, required=True)
    args = arg_parser.parse_args()

    diff_file = args.input_file

    third_party_sdk_initial = []

    with open('3rd_sdk_initial.txt') as f:
        for line in f:
            third_party_sdk_initial.append(line.strip())

    with open(diff_file) as f:
        diff_json = json.load(f)

    # Process data
    reducer = CallPathPatternReducer(diff_json, third_party_sdk_initial)
    #patterns = reducer.process_all_activities()
    # Print results
    print(json.dumps(reducer.to_json(), indent=2))

if __name__ == "__main__":
    main()