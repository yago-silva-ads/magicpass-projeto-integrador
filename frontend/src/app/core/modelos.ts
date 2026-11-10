export type Perfil = 'ADMIN' | 'VENDEDOR';
export type Categoria = 'INGRESSO' | 'PACOTE';
export type FormaPagamento = 'PIX' | 'CARTAO_CREDITO' | 'BOLETO';

export interface Entidade {
  id?: number;
  criadoEm?: string;
  atualizadoEm?: string;
  [campo: string]: any;
}

export interface Sessao {
  token: string;
  id: number;
  nome: string;
  login: string;
  perfil: Perfil;
}

export interface ErroApi {
  status: number;
  mensagem: string;
  campos?: Record<string, string> | null;
}

export interface Oferta {
  produtoId: number;
  nome: string;
  categoria: Categoria;
  parque: string;
  local: string;
  descricao: string;
  imagemUrl?: string;
  dias: number;
  noites: number;
  incluiHotel: boolean;
  incluiAereo: boolean;
  estoque: number;
  precoOriginal: number;
  precoPromocional: number;
  percentualDesconto: number;
  promocao?: string;
  parcela12x: number;
}

export interface Campanha {
  id: number;
  nome: string;
  chamada: string;
  percentual: number;
  inicio: string;
  fim: string;
  qtdMinimaProgressivo: number;
  percentualProgressivo: number;
}

export interface Vitrine {
  campanha: Campanha | null;
  cupons: { codigo: string; percentual: number; descricao: string; fim: string }[];
  ofertas: Oferta[];
  descontoPix: number;
  agora: string;
}

export interface ItemCotacao {
  produtoId: number;
  produto: string;
  categoria: Categoria;
  parque: string;
  quantidade: number;
  dataVisita: string;
  precoUnitario: number;
  percentualDesconto: number;
  precoComDesconto: number;
  totalItem: number;
}

export interface Cotacao {
  itens: ItemCotacao[];
  subtotal: number;
  descontoPromocoes: number;
  descontoPix: number;
  desconto: number;
  total: number;
  parcelas: number;
  valorParcela: number;
  promocoesAplicadas: string[];
  cupom: string | null;
}

export interface ItemRequest {
  produtoId: number | null;
  quantidade: number | null;
  dataVisita: string;
}

export interface VendaRequest {
  clienteId: number | null;
  formaPagamento: FormaPagamento;
  parcelas: number;
  cupom: string;
  observacao: string;
  itens: ItemRequest[];
}

export interface Dashboard {
  faturamento: number;
  vendasConfirmadas: number;
  vendasCanceladas: number;
  ticketMedio: number;
  economiaClientes: number;
  clientes: number;
  produtosAtivos: number;
  ingressosVendidos: number;
  maisVendidos: { produto: string; quantidade: number; faturamento: number }[];
  estoqueBaixo: { id: number; produto: string; estoque: number }[];
  campanhaAtiva: Campanha | null;
}
