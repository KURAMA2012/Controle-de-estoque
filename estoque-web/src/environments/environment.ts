/**
 * Configuração em tempo de execução: /config.js (gerado pelo container a partir de variáveis
 * de ambiente) define window.__ESTOQUE_CONFIG__. Sem ele, usa os padrões de desenvolvimento.
 */
interface ConfigRuntime {
  apiUrl?: string;
  demoLogin?: string;
  demoSenha?: string;
}

const runtime: ConfigRuntime = (globalThis as { __ESTOQUE_CONFIG__?: ConfigRuntime }).__ESTOQUE_CONFIG__ ?? {};

export const environment = {
  apiUrl: (runtime.apiUrl || 'http://localhost:7009/api/estoque').replace(/\/+$/, ''),
  /** Credenciais públicas de demonstração (somente quando o deploy de demo as informa). */
  demoLogin: runtime.demoLogin || '',
  demoSenha: runtime.demoSenha || '',
};
