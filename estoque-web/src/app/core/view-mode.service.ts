import { Injectable, effect, signal } from '@angular/core';

/** tabela = A (tabela clássica), cards = B (grade com filtros), detalhe = C (mestre-detalhe com KPIs) */
export type ViewMode = 'tabela' | 'cards' | 'detalhe';

export interface ViewModeOpcao {
  valor: ViewMode;
  rotulo: string;
  icone: string;
  descricao: string;
}

export const VIEW_MODES: ViewModeOpcao[] = [
  { valor: 'tabela', rotulo: 'Tabela', icone: 'tabela', descricao: 'Tabela clássica com ordenação e paginação' },
  { valor: 'cards', rotulo: 'Cards', icone: 'grade', descricao: 'Grade de cards com painel de filtros' },
  { valor: 'detalhe', rotulo: 'Detalhe', icone: 'split', descricao: 'Lista + detalhe com indicadores no topo' },
];

const CHAVE = 'estoque.visualizacao';

@Injectable({ providedIn: 'root' })
export class ViewModeService {
  readonly modo = signal<ViewMode>(this.inicial());

  constructor() {
    effect(() => localStorage.setItem(CHAVE, this.modo()));
  }

  definir(m: ViewMode): void {
    this.modo.set(m);
  }

  private inicial(): ViewMode {
    const v = localStorage.getItem(CHAVE);
    return v === 'tabela' || v === 'cards' || v === 'detalhe' ? v : 'tabela';
  }
}
