import MarkdownIt from 'markdown-it'

export function createMarkdownRenderer() {
  const md = new MarkdownIt({
    html: false,
    linkify: true,
    typographer: true,
    breaks: true
  })
  return md
}

export const markdownRenderer = createMarkdownRenderer()

export default markdownRenderer
