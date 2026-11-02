import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';

export type TipoToast = 'sucesso' | 'erro' | 'info';

export interface Toast {
  id: number;
  tipo: TipoToast;
  mensagem: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  sucesso(mensagem: string): void {
    this.mostrar('sucesso', mensagem);
  }

  info(mensagem: string): void {
    this.mostrar('info', mensagem);
  }

  erro(mensagem: string): void {
    this.mostrar('erro', mensagem, 7000);
  }

  /** Mostra a mensagem de erro da API (campo "message" do ExceptionResponse) ou um texto padrão por status. */
  erroApi(e: unknown, padrao = 'Não foi possível concluir a operação.'): void {
    this.erro(mensagemDeErro(e, padrao));
  }

  fechar(id: number): void {
    this.toasts.update((l) => l.filter((t) => t.id !== id));
  }

  private mostrar(tipo: TipoToast, mensagem: string, duracao = 4000): void {
    const id = ++this.seq;
    this.toasts.update((l) => [...l, { id, tipo, mensagem }]);
    setTimeout(() => this.fechar(id), duracao);
  }
}

export function mensagemDeErro(e: unknown, padrao: string): string {
  if (e instanceof HttpErrorResponse) {
    const corpo = e.error;
    const msg = corpo && typeof corpo === 'object' ? corpo.message || corpo.mensagem : null;
    if (msg && typeof msg === 'string') return msg;
    if (e.status === 0) return 'Não foi possível conectar à API.';
    if (e.status === 400) return 'Dados inválidos. Verifique os campos.';
    if (e.status === 403) return 'Você não tem permissão para esta ação.';
    if (e.status === 404) return 'Registro não encontrado.';
    if (e.status === 409) return 'Operação não permitida: o registro está em uso.';
  }
  return padrao;
}
