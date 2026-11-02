export type TipoCadastro = 'categorias' | 'marcas' | 'modelos' | 'unidades' | 'cores' | 'tamanhos';

export interface CadastroConfig {
  tipo: TipoCadastro;
  /** recurso da API: /api/estoque/{endpoint} */
  endpoint: string;
  titulo: string;
  singular: string;
  /** "Nova categoria" / "Novo modelo" */
  novo: string;
  icone: string;
  placeholderDescricao: string;
  /** especialização de Modelo: vínculo com marca */
  usaMarca?: boolean;
}

export const CADASTROS: Record<TipoCadastro, CadastroConfig> = {
  categorias: { tipo: 'categorias', endpoint: 'categoria', titulo: 'Categorias', singular: 'categoria', novo: 'Nova categoria', icone: 'categorias', placeholderDescricao: 'Ex.: Eletrônicos' },
  marcas: { tipo: 'marcas', endpoint: 'marca', titulo: 'Marcas', singular: 'marca', novo: 'Nova marca', icone: 'marcas', placeholderDescricao: 'Ex.: Samsung' },
  modelos: { tipo: 'modelos', endpoint: 'modelo', titulo: 'Modelos', singular: 'modelo', novo: 'Novo modelo', icone: 'modelos', placeholderDescricao: 'Ex.: Galaxy S24', usaMarca: true },
  unidades: { tipo: 'unidades', endpoint: 'unidade', titulo: 'Unidades', singular: 'unidade', novo: 'Nova unidade', icone: 'unidades', placeholderDescricao: 'Ex.: Unidade, Caixa, Kg' },
  cores: { tipo: 'cores', endpoint: 'cor', titulo: 'Cores', singular: 'cor', novo: 'Nova cor', icone: 'cores', placeholderDescricao: 'Ex.: Preto' },
  tamanhos: { tipo: 'tamanhos', endpoint: 'tamanho', titulo: 'Tamanhos', singular: 'tamanho', novo: 'Novo tamanho', icone: 'tamanhos', placeholderDescricao: 'Ex.: M, 42, 500ml' },
};
