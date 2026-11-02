import { Injectable, signal } from '@angular/core';

export interface OpcoesConfirmacao {
  titulo: string;
  mensagem: string;
  confirmar?: string;
  cancelar?: string;
  perigo?: boolean;
}

interface Pedido extends OpcoesConfirmacao {
  resolver: (ok: boolean) => void;
}

/** Diálogo de confirmação do app (substitui window.confirm). */
@Injectable({ providedIn: 'root' })
export class ConfirmacaoService {
  readonly atual = signal<Pedido | null>(null);

  perguntar(opcoes: OpcoesConfirmacao): Promise<boolean> {
    this.atual()?.resolver(false);
    return new Promise<boolean>((resolver) => this.atual.set({ perigo: true, ...opcoes, resolver }));
  }

  responder(ok: boolean): void {
    const p = this.atual();
    if (!p) return;
    this.atual.set(null);
    p.resolver(ok);
  }
}
