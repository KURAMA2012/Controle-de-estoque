import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { environment } from '../../environments/environment';

/** Referência a um cadastro auxiliar (categoria, marca, modelo, unidade, cor, tamanho). */
export interface Ref {
  id: number;
  descricao: string;
  ativo?: boolean;
}

/** Campos reais do JSON de Item (domain/Item.java). Os bytes da imagem não vêm no JSON: use possuiImagem/versaoImagem + GET item/{id}/imagem-principal. */
export interface Item {
  id: number;
  nome: string;
  descricao?: string | null;
  codigoBarras?: string | null;
  codigoFornecedor?: string | null;
  possuiImagem?: boolean;
  versaoImagem?: string | null;
  icone?: string | null;
  unidade?: Ref | null;
  idCategoria?: Ref | null;
  idMarca?: Ref | null;
  idModelo?: Ref | null;
  quantidadeEstoqueMinimo?: number | null;
  quantidadeEstoqueMaximo?: number | null;
  quantidadeEstoqueAtual?: number | null;
  ativo: boolean;
  valorCusto?: number | null;
  margemLucro?: number | null;
  valorVenda?: number | null;
  precoAntigo?: number | null;
  maxParcelas?: number | null;
  full?: boolean;
  freteGratis?: boolean;
  rating?: number | null;
  qtdReviews?: number | null;
  qtdVendidos?: number | null;
  data_validade?: string | null;
  cores?: Ref[] | null;
  tamanhos?: Ref[] | null;
}

/** Corpo de item/inserir e item/{id}/atualizar (dto/ItemRequestDTO.java). */
export interface ItemRequest {
  id?: number | null;
  nome: string;
  idUnidade?: number | null;
  idCategoria?: number | null;
  idMarca?: number | null;
  idModelo?: number | null;
  codigoBarras?: string | null;
  icone?: string | null;
  descricao?: string | null;
  dataValidade?: string | null;
  quantidadeEstoqueMinimo?: number | null;
  quantidadeEstoqueMaximo?: number | null;
  quantidadeEstoqueAtual?: number | null;
  ativo: boolean;
  valorCusto?: number | null;
  margemLucro?: number | null;
  valorVenda?: number | null;
  precoAntigo?: number | null;
  maxParcelas?: number | null;
  full: boolean;
  freteGratis: boolean;
  rating?: number | null;
  qtdReviews?: number | null;
  qtdVendidos?: number | null;
  codigoFornecedor?: string | null;
  idsCores: number[];
  idsTamanhos: number[];
}

export interface ItemImagem {
  id: number;
  idItem: number;
  ordem: number;
  imagem: string | null;
  url: string | null;
  principal: boolean;
}

export interface ItemEspecificacao {
  id: number;
  idItem: number;
  chave: string;
  valor: string | null;
}

/** Filtros aceitos pelo ConsultaPaginada: String = LIKE, boolean = igual, objeto {id} = igual pelo id. */
export interface FiltrosItem {
  nome?: string;
  ativo?: boolean;
  idCategoria?: { id: number };
  idMarca?: { id: number };
}

export interface RequestPage {
  filtros: FiltrosItem;
  orderBy?: string;
  /** 'asc' (padrão) ou 'desc'. */
  direcao?: 'asc' | 'desc';
  page: number;
  size: number;
}

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export type NivelEstoque = 'sem' | 'baixo' | 'ok';

export function nivelEstoque(it: Item): NivelEstoque {
  const atual = it.quantidadeEstoqueAtual ?? 0;
  if (atual <= 0) return 'sem';
  if (it.quantidadeEstoqueMinimo != null && atual <= it.quantidadeEstoqueMinimo) return 'baixo';
  return 'ok';
}

/** Converte os bytes (Base64) vindos da API em data URL, detectando o tipo pelos primeiros bytes. */
export function srcImagem(b64?: string | null): string | null {
  if (!b64) return null;
  const mime = b64.startsWith('iVBOR')
    ? 'image/png'
    : b64.startsWith('R0lG')
      ? 'image/gif'
      : b64.startsWith('UklG')
        ? 'image/webp'
        : 'image/jpeg';
  return `data:${mime};base64,${b64}`;
}

@Injectable({ providedIn: 'root' })
export class ItemService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/item`;
  private readonly cacheImagens = new Map<number, Observable<ItemImagem[]>>();
  private readonly cacheEspecificacoes = new Map<number, Observable<ItemEspecificacao[]>>();

  consultar(req: RequestPage): Observable<Pagina<Item>> {
    return this.http.post<Pagina<Item>>(`${this.base}/consultar`, req);
  }

  detalhar(id: number): Observable<Item> {
    return this.http.get<Item>(`${this.base}/detalhar/${id}`);
  }

  /** Galeria (com bytes): carregada só quando o item é aberto no detalhe e mantida em cache. */
  imagens(id: number): Observable<ItemImagem[]> {
    let obs = this.cacheImagens.get(id);
    if (!obs) {
      obs = this.http.get<ItemImagem[]>(`${this.base}/${id}/imagens`).pipe(shareReplay({ bufferSize: 1, refCount: false }));
      this.cacheImagens.set(id, obs);
    }
    return obs;
  }

  especificacoes(id: number): Observable<ItemEspecificacao[]> {
    let obs = this.cacheEspecificacoes.get(id);
    if (!obs) {
      obs = this.http
        .get<ItemEspecificacao[]>(`${this.base}/${id}/especificacoes`)
        .pipe(shareReplay({ bufferSize: 1, refCount: false }));
      this.cacheEspecificacoes.set(id, obs);
    }
    return obs;
  }

  inserir(req: ItemRequest): Observable<Item> {
    return this.http.post<Item>(`${this.base}/inserir`, req);
  }

  atualizar(id: number, req: ItemRequest): Observable<Item> {
    return this.http.put<Item>(`${this.base}/${id}/atualizar`, req);
  }

  anexarImagem(id: number, arquivo: File): Observable<unknown> {
    const fd = new FormData();
    fd.append('arquivo', arquivo);
    return this.http.post(`${this.base}/anexar-imagem/${id}`, fd);
  }

  adicionarImagemUrl(id: number, url: string): Observable<unknown> {
    return this.http.post(`${this.base}/${id}/imagens/url`, { url });
  }

  removerImagem(idImagem: number): Observable<unknown> {
    return this.http.delete(`${this.base}/imagens/${idImagem}`);
  }

  definirPrincipal(idImagem: number): Observable<unknown> {
    return this.http.put(`${this.base}/imagens/${idImagem}/definir-principal`, {});
  }

  adicionarEspecificacao(id: number, chave: string, valor: string | null): Observable<ItemEspecificacao> {
    return this.http.post<ItemEspecificacao>(`${this.base}/${id}/especificacoes`, { chave, valor });
  }

  atualizarEspecificacao(idEsp: number, chave: string, valor: string | null): Observable<ItemEspecificacao> {
    return this.http.put<ItemEspecificacao>(`${this.base}/especificacoes/${idEsp}`, { chave, valor });
  }

  removerEspecificacao(idEsp: number): Observable<unknown> {
    return this.http.delete(`${this.base}/especificacoes/${idEsp}`);
  }

  deletar(id: number): Observable<boolean> {
    return this.http.delete<boolean>(`${this.base}/deletar/${id}`);
  }

  limparCache(id?: number): void {
    if (id == null) {
      this.cacheImagens.clear();
      this.cacheEspecificacoes.clear();
    } else {
      this.cacheImagens.delete(id);
      this.cacheEspecificacoes.delete(id);
    }
  }
}
