var myHeaders = new Headers();
myHeaders.append("Authorization", "Bearer sk-8db834dd2164453388e989baca30c453");
myHeaders.append("Content-Type", "application/json");

var raw = JSON.stringify({
   "model": "deepseek-chat",
   "messages": [
      {
         "role": "system",
         "content": "You are a helpful assistant."
      },
      {
         "role": "user",
         "content": "简单介绍你自己!"
      }
   ],
   "stream": true
});

var requestOptions = {
   method: 'POST',
   headers: myHeaders,
   body: raw,
   redirect: 'follow'
};

fetch("https://api.deepseek.com/chat/completions", requestOptions)
   .then(response => response.text())
   .then(result => console.log(result))
   .catch(error => console.log('error', error));

流式返回
```
data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"role":"assistant","content":""},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"你好"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"！"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"我是"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"你的"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"智能"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"助手"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"，"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"随时"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"为你"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"解答"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"问题"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"、"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"提供"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"帮助"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"，"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"无论是"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"学习"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"、"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"工作"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"还是"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"生活"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"琐"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"事"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"，"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"有"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"问"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"必"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"答"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":"～"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":" 😊"},"logprobs":null,"finish_reason":null}]}

data: {"id":"5e62e604-6195-4e7f-aeb4-f9918935b542","object":"chat.completion.chunk","created":1752024306,"model":"deepseek-chat","system_fingerprint":"fp_8802369eaa_prod0623_fp8_kvcache","choices":[{"index":0,"delta":{"content":""},"logprobs":null,"finish_reason":"stop"}],"usage":{"prompt_tokens":14,"completion_tokens":31,"total_tokens":45,"prompt_tokens_details":{"cached_tokens":0},"prompt_cache_hit_tokens":0,"prompt_cache_miss_tokens":14}}

data: [DONE]


```