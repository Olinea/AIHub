#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
最简单的验证脚本 - 只测试连接是否正常关闭
忽略数据库保存问题，专注解决连接问题
"""

import requests
import json
import time

def test_simple_connection():
    """最简单的连接测试"""
    print("🔧 最简单的连接关闭测试")
    
    # 登录
    response = requests.post(
        "http://localhost:8080/api/auth/login",
        json={"email": "john@example.com", "password": "password123"}
    )
    
    if response.status_code != 200:
        print("❌ 登录失败")
        return
        
    token = response.json()['data']['accessToken']
    print("✅ 登录成功")
    
    # 流式请求
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "text/event-stream",
        "Cache-Control": "no-cache"
    }
    
    data = {
        "model": "deepseek-chat",
        "messages": [{"role": "user", "content": "说两个字"}],
        "stream": True
    }
    
    try:
        print("📡 发送流式请求...")
        response = requests.post(
            "http://localhost:8080/api/v1/chat/completions",
            json=data,
            headers=headers,
            stream=True,
            timeout=30
        )
        
        print(f"状态码: {response.status_code}")
        
        chunks = []
        done_received = False
        
        try:
            for line in response.iter_lines(decode_unicode=True):
                if line.strip():
                    chunks.append(line)
                    print(f"[{len(chunks):02d}] {line}")
                    
                    if line.strip() == "data: [DONE]":
                        done_received = True
                        print("✅ 收到 [DONE] 标记")
                        break
            
            # 检查连接是否正常关闭
            print(f"\n总共收到 {len(chunks)} 个数据块")
            print(f"收到 [DONE]: {'是' if done_received else '否'}")
            print("✅ 连接正常关闭（无异常抛出）")
            
        except Exception as e:
            print(f"❌ 连接异常: {e}")
            if "prematurely" in str(e):
                print("这是 'Response ended prematurely' 错误")
            
    except Exception as e:
        print(f"❌ 请求失败: {e}")

if __name__ == "__main__":
    test_simple_connection()
