const API_BASE = import.meta.env.VITE_API_BASE || ''

export interface StreamChatOptions {
  url: string
  payload: any
  onMessage: (chunk: string) => void
  onError?: (err: any) => void
  onComplete?: () => void
  signal?: AbortSignal
}

/**
 * 生产级 LLM 流式请求封装
 */
export async function streamChat({
  url,
  payload,
  onMessage,
  onError,
  onComplete,
  signal
}: StreamChatOptions) {
  try {
    const res = await fetch(`${API_BASE}${url}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(payload),
      signal
    })

    if (!res.ok || !res.body) {
      throw new Error(`HTTP ${res.status}`)
    }

    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')

    let buffer = '' // ⭐ 关键：跨 chunk 缓冲

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      // ⭐ 按 SSE 事件分割（\n\n），解决TCP粘包拆包问题。
      const events = buffer.split('\n\n')
      buffer = events.pop() || '' // 无法判断最后一段是否完整，留给下次裁剪

      for (const event of events) {
        const line = event.trim()
        console.log('line:', line)
        if (!line.startsWith('data:')) continue

        const jsonStr = line.replace(/^data:\s*/, '')
        if (!jsonStr) continue

        try {
          const payload = JSON.parse(jsonStr)

          if (payload.type === 'delta' && payload.content) {
            onMessage(payload.content)
          }
        } catch (e) {
          console.warn('SSE JSON parse error:', jsonStr)
        }
      }
    }

    onComplete?.()
  } catch (err: any) {
    if (err.name !== 'AbortError') {
      onError?.(err)
    }
  }
}

