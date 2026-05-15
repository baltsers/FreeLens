import ollama
import anthropic
from openai import OpenAI
from abc import ABC, abstractmethod
from pathlib import Path




class LLM(ABC):
    @abstractmethod
    def chat(self, prompt, system_message, exact_model_name):
        pass


class LLama3(LLM):
    def chat(self, prompt, system_message, exact_model_name):
        messages = [
            {'role': 'system', 'content': system_message},
            {'role': 'user', 'content': prompt}
        ]
        try:
            response = ollama.chat(model=exact_model_name, messages=messages, stream=False, options=dict(num_ctx=8192))
        except ollama.ResponseError as e:
            error_message = f"Error: {e.error}, Status Code: {e.status_code}"
            print(error_message)
        response_message = response['message']['content']
        return response_message


class Claude(LLM):
    def chat(self, prompt, system_message, exact_model_name):
        client = anthropic.Anthropic()
        message = client.messages.create(
            model="claude-3-5-sonnet-20241022",
            max_tokens=2000,
            temperature=0,
            system=system_message,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": prompt
                        }
                    ]
                }
            ]
        )
        return message.content[0].text


class ChatGPT(LLM):
    def chat(self, prompt, system_message, exact_model_name):
        client = OpenAI()
        messages = [
            {"role": "system", "content": system_message},
            {"role": "user", "content": prompt}
        ]
        completion = client.chat.completions.create(
            model=exact_model_name,
            messages=messages
        )
        response_message = completion.choices[0].message.content
        return response_message


class Summarizer:
    def __init__(self, model, exact_model_name):
        self._model = model
        self._exact_model_name = exact_model_name
        self._system_message = ""
        self._results = []
        self._synthetic_result = ""

    def set_model(self, model):
        self._model = model

    def set_system_message(self, system_message):
        self._system_message = system_message

    # def __extract_summarization(self, response_message):
    #     # In the response message, the real summarization is within the '{}' brackets
    #     if '{' not in response_message:
    #         return response_message
    #     return response_message.split('{')[1].split('}')[0]

    def summarize(self, prompt):
        response_message = self._model.chat(prompt, self._system_message, self._exact_model_name)
        self._results.append(response_message)
        return response_message

    def get_summarization_results(self):
        return self._results

    def final_summarization(self, final_prompt):
        self._synthetic_result = self._model.chat(final_prompt, self._system_message, self._exact_model_name)
        return self._synthetic_result

    def serialize_results(self, path):
        with open(path, 'w') as f:
            for i, result in enumerate(self._results, start=1):
                f.write(f"Summarization {i}. {result}\n")
        with open(path + ".final", 'w') as f:
            f.write(f"Synthetic Summarization:\n {self._sythetic_result}\n")
