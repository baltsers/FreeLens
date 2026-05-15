import os
import argparse
import sys

import llm_assistant
from pathlib import Path

# Get project root (2 levels up from experiments/YYYY-MM-DD)
PROJECT_ROOT = Path(__file__).parents[2]
SYSTEM_MESSAGE_DIR = f'{PROJECT_ROOT}/llm/system-instructions'


def model_factory(model_name):
    if model_name == 'LLama3':
        return llm_assistant.LLama3()
    elif model_name == 'Claude':
        return llm_assistant.Claude()
    else:
        return llm_assistant.ChatGPT()


def system_message_factory(category):
    with open(f'{SYSTEM_MESSAGE_DIR}/callpath_system_message.txt', 'r') as file:
        return file.read()


def compose_whole_prompt(prompt, system_message):
    # Ollama model sometimes will ignore the system message, so we need to append it to the prompt
    return f"{system_message}\n{prompt}"


def summarize_diff(args, llm_chatbot):
    official_prompt_file = ''
    for file_name in os.listdir(args.input_dir):
        if "official_prompt.txt" in file_name:
            official_prompt_file = file_name
            break

    if not official_prompt_file:
        print(f"Official prompt file not found in {args.input_dir}, please consider using other prompts.")
        sys.exit(1)

    system_message = system_message_factory('official')
    llm_chatbot.set_system_message(system_message)
    with open(os.path.join(args.input_dir, official_prompt_file), 'r') as file:
        prompt = file.read()
    response = llm_chatbot.summarize(prompt)
    single_summary_file = os.path.join(args.output_dir, f'{args.model}_summary.txt')
    single_prompt_and_summary_file = single_summary_file + '.debug'
    with open(single_summary_file, 'w') as f:
        f.write(f"{response}\n")
    with open(single_prompt_and_summary_file, 'w') as f:
        f.write(f"[Prompt]\n\n{prompt}\n\n[Summary]\n\n{response}\n")



def main():
    parser = argparse.ArgumentParser(description="Chat with a language model.")
    parser.add_argument('--input-dir', type=str, required=True)
    parser.add_argument('--model', type=str, required=False, choices=['LLama3', 'Claude', 'ChatGPT'], default='LLama3')
    parser.add_argument('--exact-model-name', type=str, default="qwen2.5:72b-instruct")
    parser.add_argument('--output-dir', type=str, required=True)

    args = parser.parse_args()

    input_dir = args.input_dir
    # Check if input directory is empty
    if not os.listdir(input_dir):
        print(f"Input directory {input_dir} is empty, please consider using other prompts.")
        return

    model = model_factory(args.model)
    llm_chatbot = llm_assistant.Summarizer(model, args.exact_model_name)

    os.makedirs(args.output_dir, exist_ok=True)


    summarize_diff(args, llm_chatbot)


if __name__ == "__main__":
    main()
