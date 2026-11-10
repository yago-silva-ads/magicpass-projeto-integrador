import { Entidade } from '../../core/modelos';
import {
  cnpjValido, cpfValido, ieValida, mascaraCnpj, mascaraCpf, mascaraRg, mascaraTelefone, rgValido,
} from '../../core/validadores';

export type TipoCampo =
  | 'texto' | 'numero' | 'dinheiro' | 'email' | 'data' | 'datahora' | 'select' | 'ref'
  | 'check' | 'area' | 'senha' | 'cpf' | 'cnpj' | 'rg' | 'ie' | 'telefone' | 'uf';

export interface Campo {
  nome: string;
  rotulo: string;
  tipo: TipoCampo;
  obrigatorio?: boolean;
  opcoes?: { valor: any; rotulo: string }[];
  ref?: { recurso: string; rotulo: (e: Entidade) => string };
  min?: number;
  max?: number;
  dica?: string;
  inteira?: boolean;
  placeholder?: string;
  visivel?: (m: any) => boolean;
  validar?: (v: any, m: any, editando: boolean) => string | null;
}

export interface Coluna {
  rotulo: string;
  valor: (e: Entidade) => string | number | null | undefined;
  classe?: (e: Entidade) => string;
  alinhar?: 'dir';
}

export interface CrudConfig {
  titulo: string;
  singular: string;
  subtitulo: string;
  recurso: string;
  icone: string;
  somenteAdminEdita?: boolean;
  colunas: Coluna[];
  campos: Campo[];
  novo: () => Record<string, any>;
  nomeRegistro: (e: Entidade) => string;
}

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
export const fmtMoeda = (v: any) => (v == null ? '-' : moeda.format(Number(v)));
export const fmtData = (v: any) => (v ? new Date(String(v).length <= 10 ? v + 'T00:00:00' : v).toLocaleDateString('pt-BR') : '-');
export const fmtDataHora = (v: any) =>
  v ? new Date(v).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '-';

const UFS = ['AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA', 'PB', 'PE', 'PI', 'PR',
  'RJ', 'RN', 'RO', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO'].map((u) => ({ valor: u, rotulo: u }));

const ativoBadge = (e: Entidade) => (e['ativo'] || e['ativa'] ? 'badge ok' : 'badge off');
const simNao = (b: any) => (b ? 'Ativo' : 'Inativo');

// ------------------------------------------------------------------ Clientes
export const CLIENTES: CrudConfig = {
  titulo: 'Clientes',
  singular: 'cliente',
  subtitulo: 'Viajantes que compram ingressos e pacotes',
  recurso: 'clientes',
  icone: 'clientes',
  nomeRegistro: (e) => e['nome'],
  novo: () => ({ nome: '', cpf: '', rg: '', email: '', telefone: '', dataNascimento: '', cidade: '', uf: 'SP' }),
  colunas: [
    { rotulo: 'Nome', valor: (e) => e['nome'] },
    { rotulo: 'CPF', valor: (e) => mascaraCpf(e['cpf']) },
    { rotulo: 'RG', valor: (e) => mascaraRg(e['rg']) },
    { rotulo: 'E-mail', valor: (e) => e['email'] },
    { rotulo: 'Telefone', valor: (e) => mascaraTelefone(e['telefone']) },
    { rotulo: 'Cidade/UF', valor: (e) => [e['cidade'], e['uf']].filter(Boolean).join(' / ') },
  ],
  campos: [
    { nome: 'nome', rotulo: 'Nome completo', tipo: 'texto', obrigatorio: true, inteira: true,
      validar: (v) => (String(v ?? '').trim().length < 3 ? 'Nome deve ter pelo menos 3 caracteres' : null) },
    { nome: 'cpf', rotulo: 'CPF', tipo: 'cpf', obrigatorio: true, placeholder: '000.000.000-00',
      validar: (v) => (cpfValido(v) ? null : 'CPF invalido (digitos verificadores nao conferem)') },
    { nome: 'rg', rotulo: 'RG (SP)', tipo: 'rg', obrigatorio: true, placeholder: '00.000.000-0',
      dica: 'Ultimo digito pode ser X',
      validar: (v) => (rgValido(v) ? null : 'RG invalido (digito verificador nao confere)') },
    { nome: 'email', rotulo: 'E-mail', tipo: 'email', obrigatorio: true,
      validar: (v) => (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v ?? '') ? null : 'E-mail invalido') },
    { nome: 'telefone', rotulo: 'Celular', tipo: 'telefone', placeholder: '(11) 91234-5678',
      validar: (v) => { const d = String(v ?? '').replace(/\D/g, ''); return !d || d.length === 10 || d.length === 11 ? null : 'Informe DDD + numero'; } },
    { nome: 'dataNascimento', rotulo: 'Nascimento', tipo: 'data',
      validar: (v) => (v && new Date(v) >= new Date() ? 'Data deve estar no passado' : null) },
    { nome: 'cidade', rotulo: 'Cidade', tipo: 'texto' },
    { nome: 'uf', rotulo: 'UF', tipo: 'uf', opcoes: UFS },
  ],
};

// ------------------------------------------------------------------ Parceiros
export const PARCEIROS: CrudConfig = {
  titulo: 'Parceiros',
  singular: 'parceiro',
  subtitulo: 'Operadoras, hoteis e receptivos que compoem os pacotes',
  recurso: 'parceiros',
  icone: 'parceiros',
  somenteAdminEdita: true,
  nomeRegistro: (e) => e['razaoSocial'],
  novo: () => ({ razaoSocial: '', nomeFantasia: '', cnpj: '', inscricaoEstadual: '', uf: 'SP', tipoServico: '', email: '', telefone: '' }),
  colunas: [
    { rotulo: 'Razao social', valor: (e) => e['razaoSocial'] },
    { rotulo: 'CNPJ', valor: (e) => mascaraCnpj(e['cnpj']) },
    { rotulo: 'IE', valor: (e) => e['inscricaoEstadual'] },
    { rotulo: 'UF', valor: (e) => e['uf'] },
    { rotulo: 'Servico', valor: (e) => e['tipoServico'] },
  ],
  campos: [
    { nome: 'razaoSocial', rotulo: 'Razao social', tipo: 'texto', obrigatorio: true, inteira: true },
    { nome: 'nomeFantasia', rotulo: 'Nome fantasia', tipo: 'texto' },
    { nome: 'cnpj', rotulo: 'CNPJ', tipo: 'cnpj', obrigatorio: true, placeholder: '00.000.000/0000-00',
      dica: 'Aceita o novo CNPJ alfanumerico',
      validar: (v) => (cnpjValido(v) ? null : 'CNPJ invalido (digitos verificadores nao conferem)') },
    { nome: 'uf', rotulo: 'UF', tipo: 'uf', obrigatorio: true, opcoes: UFS },
    { nome: 'inscricaoEstadual', rotulo: 'Inscricao estadual', tipo: 'ie', obrigatorio: true,
      dica: 'Valida SP, RJ, PR e SC. Outras UFs: ISENTO',
      validar: (v, m) => (ieValida(v, m.uf) ? null : `IE invalida para ${m.uf || 'a UF'} (ou digite ISENTO)`) },
    { nome: 'tipoServico', rotulo: 'Tipo de servico', tipo: 'texto', obrigatorio: true, placeholder: 'Ex.: Aereo + hotel' },
    { nome: 'email', rotulo: 'E-mail', tipo: 'email' },
    { nome: 'telefone', rotulo: 'Telefone', tipo: 'telefone' },
  ],
};

// ------------------------------------------------------------------ Parques
export const PARQUES: CrudConfig = {
  titulo: 'Parques',
  singular: 'parque',
  subtitulo: 'Destinos Disney disponiveis para venda',
  recurso: 'parques',
  icone: 'castelo',
  somenteAdminEdita: true,
  nomeRegistro: (e) => e['nome'],
  novo: () => ({ nome: '', local: '', descricao: '', imagemUrl: '', ativo: true }),
  colunas: [
    { rotulo: 'Parque', valor: (e) => e['nome'] },
    { rotulo: 'Local', valor: (e) => e['local'] },
    { rotulo: 'Descricao', valor: (e) => e['descricao'] },
    { rotulo: 'Status', valor: (e) => simNao(e['ativo']), classe: ativoBadge },
  ],
  campos: [
    { nome: 'nome', rotulo: 'Nome do parque', tipo: 'texto', obrigatorio: true },
    { nome: 'local', rotulo: 'Local', tipo: 'texto', obrigatorio: true, placeholder: 'Ex.: Orlando - Walt Disney World' },
    { nome: 'descricao', rotulo: 'Descricao', tipo: 'area', inteira: true },
    { nome: 'imagemUrl', rotulo: 'URL da imagem (opcional)', tipo: 'texto', inteira: true },
    { nome: 'ativo', rotulo: 'Ativo', tipo: 'check' },
  ],
};

// ------------------------------------------------------------------ Produtos
export const PRODUTOS: CrudConfig = {
  titulo: 'Ingressos e Pacotes',
  singular: 'produto',
  subtitulo: 'Catalogo vendido no balcao e na vitrine Black Friday',
  recurso: 'produtos',
  icone: 'ingresso',
  somenteAdminEdita: true,
  nomeRegistro: (e) => e['nome'],
  novo: () => ({ nome: '', categoria: 'INGRESSO', parque: null, parceiro: null, descricao: '', preco: null, dias: 1,
    noites: 0, incluiHotel: false, incluiAereo: false, estoque: 0, ativo: true, imagemUrl: '' }),
  colunas: [
    { rotulo: 'Produto', valor: (e) => e['nome'] },
    { rotulo: 'Tipo', valor: (e) => (e['categoria'] === 'PACOTE' ? 'Pacote' : 'Ingresso'),
      classe: (e) => (e['categoria'] === 'PACOTE' ? 'badge roxo' : 'badge azul') },
    { rotulo: 'Parque', valor: (e) => e['parque']?.nome },
    { rotulo: 'Dias', valor: (e) => e['dias'] + (e['noites'] ? ` / ${e['noites']}n` : '') },
    { rotulo: 'Preco', valor: (e) => fmtMoeda(e['preco']), alinhar: 'dir' },
    { rotulo: 'Estoque', valor: (e) => e['estoque'], alinhar: 'dir',
      classe: (e) => (e['estoque'] < 10 ? 'badge alerta' : '') },
    { rotulo: 'Status', valor: (e) => simNao(e['ativo']), classe: ativoBadge },
  ],
  campos: [
    { nome: 'nome', rotulo: 'Nome', tipo: 'texto', obrigatorio: true, inteira: true },
    { nome: 'categoria', rotulo: 'Categoria', tipo: 'select', obrigatorio: true,
      opcoes: [{ valor: 'INGRESSO', rotulo: 'Ingresso' }, { valor: 'PACOTE', rotulo: 'Pacote de viagem' }] },
    { nome: 'parque', rotulo: 'Parque', tipo: 'ref', obrigatorio: true, ref: { recurso: 'parques', rotulo: (e) => e['nome'] } },
    { nome: 'parceiro', rotulo: 'Parceiro (operadora)', tipo: 'ref', ref: { recurso: 'parceiros', rotulo: (e) => e['nomeFantasia'] || e['razaoSocial'] },
      visivel: (m) => m.categoria === 'PACOTE' },
    { nome: 'preco', rotulo: 'Preco (R$)', tipo: 'dinheiro', obrigatorio: true, min: 0.01,
      validar: (v) => (v == null || v === '' || Number(v) <= 0 ? 'Preco deve ser maior que zero' : null) },
    { nome: 'estoque', rotulo: 'Estoque', tipo: 'numero', obrigatorio: true, min: 0,
      validar: (v) => (v == null || v === '' || Number(v) < 0 || !Number.isInteger(Number(v)) ? 'Estoque deve ser inteiro e nao negativo' : null) },
    { nome: 'dias', rotulo: 'Dias', tipo: 'numero', obrigatorio: true, min: 1, max: 30,
      validar: (v) => (!Number.isInteger(Number(v)) || Number(v) < 1 || Number(v) > 30 ? 'Dias entre 1 e 30' : null) },
    { nome: 'noites', rotulo: 'Noites', tipo: 'numero', min: 0, max: 30, visivel: (m) => m.categoria === 'PACOTE',
      validar: (v, m) => (Number(v) < 0 ? 'Noites nao pode ser negativo' : m.incluiHotel && Number(v) < 1 ? 'Pacote com hotel precisa de 1+ noite' : Number(v) > Number(m.dias) ? 'Noites maior que dias' : null) },
    { nome: 'incluiHotel', rotulo: 'Inclui hotel', tipo: 'check', visivel: (m) => m.categoria === 'PACOTE' },
    { nome: 'incluiAereo', rotulo: 'Inclui aereo', tipo: 'check', visivel: (m) => m.categoria === 'PACOTE' },
    { nome: 'descricao', rotulo: 'Descricao', tipo: 'area', inteira: true },
    { nome: 'ativo', rotulo: 'Ativo (aparece para venda)', tipo: 'check' },
  ],
};

// ------------------------------------------------------------------ Promocoes
export const PROMOCOES: CrudConfig = {
  titulo: 'Promocoes',
  singular: 'promocao',
  subtitulo: 'Campanhas Black Friday, cupons e desconto progressivo para familias',
  recurso: 'promocoes',
  icone: 'promocao',
  somenteAdminEdita: true,
  nomeRegistro: (e) => e['nome'],
  novo: () => ({ nome: '', chamada: '', percentual: 10, inicio: '', fim: '', alvo: 'TODOS', produto: null, cupom: '',
    qtdMinimaProgressivo: 0, percentualProgressivo: 0, ativa: true }),
  colunas: [
    { rotulo: 'Promocao', valor: (e) => e['nome'] },
    { rotulo: 'Desconto', valor: (e) => `${e['percentual']}%`, alinhar: 'dir' },
    { rotulo: 'Vale para', valor: (e) => (e['alvo'] === 'PRODUTO' ? e['produto']?.nome : ({ TODOS: 'Todos', INGRESSO: 'Ingressos', PACOTE: 'Pacotes' } as any)[e['alvo']]) },
    { rotulo: 'Cupom', valor: (e) => e['cupom'] || '-' },
    { rotulo: 'Familia', valor: (e) => (e['qtdMinimaProgressivo'] ? `${e['qtdMinimaProgressivo']}+ itens: +${e['percentualProgressivo']}%` : '-') },
    { rotulo: 'Periodo', valor: (e) => `${fmtDataHora(e['inicio'])} ate ${fmtDataHora(e['fim'])}` },
    { rotulo: 'Status', valor: (e) => statusPromo(e), classe: (e) => (statusPromo(e) === 'No ar' ? 'badge ok' : 'badge off') },
  ],
  campos: [
    { nome: 'nome', rotulo: 'Nome', tipo: 'texto', obrigatorio: true },
    { nome: 'percentual', rotulo: 'Desconto (%)', tipo: 'numero', obrigatorio: true, min: 1, max: 70,
      validar: (v) => (v == null || v === '' || Number(v) < 1 || Number(v) > 70 ? 'Entre 1% e 70%' : null) },
    { nome: 'chamada', rotulo: 'Chamada para a vitrine', tipo: 'texto', inteira: true },
    { nome: 'inicio', rotulo: 'Inicio', tipo: 'datahora', obrigatorio: true },
    { nome: 'fim', rotulo: 'Fim', tipo: 'datahora', obrigatorio: true,
      validar: (v, m) => (v && m.inicio && new Date(v) <= new Date(m.inicio) ? 'Fim deve ser depois do inicio' : null) },
    { nome: 'alvo', rotulo: 'Vale para', tipo: 'select', obrigatorio: true,
      opcoes: [{ valor: 'TODOS', rotulo: 'Todos os produtos' }, { valor: 'INGRESSO', rotulo: 'Somente ingressos' },
        { valor: 'PACOTE', rotulo: 'Somente pacotes' }, { valor: 'PRODUTO', rotulo: 'Um produto especifico' }] },
    { nome: 'produto', rotulo: 'Produto', tipo: 'ref', obrigatorio: true, ref: { recurso: 'produtos', rotulo: (e) => e['nome'] },
      visivel: (m) => m.alvo === 'PRODUTO' },
    { nome: 'cupom', rotulo: 'Cupom (opcional)', tipo: 'texto', placeholder: 'Ex.: BLACKMAGIC',
      dica: 'Se preenchido, o desconto so vale com o cupom',
      validar: (v) => (v && !/^[A-Z0-9]{3,20}$/.test(String(v).toUpperCase()) ? '3 a 20 letras/numeros' : null) },
    { nome: 'qtdMinimaProgressivo', rotulo: 'Familia: a partir de (itens)', tipo: 'numero', min: 0, max: 50,
      validar: (v) => (Number(v) < 0 ? 'Nao pode ser negativo' : null) },
    { nome: 'percentualProgressivo', rotulo: 'Familia: desconto extra (%)', tipo: 'numero', min: 0, max: 20,
      validar: (v, m) => (Number(v) < 0 || Number(v) > 20 ? 'Entre 0% e 20%' : (Number(v) > 0) !== (Number(m.qtdMinimaProgressivo) > 0) ? 'Preencha quantidade e percentual juntos' : null) },
    { nome: 'ativa', rotulo: 'Ativa', tipo: 'check' },
  ],
};

function statusPromo(e: Entidade) {
  const agora = new Date();
  if (!e['ativa']) return 'Inativa';
  if (new Date(e['inicio']) > agora) return 'Agendada';
  if (new Date(e['fim']) < agora) return 'Encerrada';
  return 'No ar';
}

// ------------------------------------------------------------------ Usuarios
export const USUARIOS: CrudConfig = {
  titulo: 'Usuarios',
  singular: 'usuario',
  subtitulo: 'Logins e senhas cadastrados pelo administrador',
  recurso: 'usuarios',
  icone: 'usuarios',
  nomeRegistro: (e) => e['login'],
  novo: () => ({ nome: '', login: '', senha: '', perfil: 'VENDEDOR', ativo: true }),
  colunas: [
    { rotulo: 'Nome', valor: (e) => e['nome'] },
    { rotulo: 'Login', valor: (e) => e['login'] },
    { rotulo: 'Perfil', valor: (e) => (e['perfil'] === 'ADMIN' ? 'Administrador' : 'Vendedor'),
      classe: (e) => (e['perfil'] === 'ADMIN' ? 'badge roxo' : 'badge azul') },
    { rotulo: 'Status', valor: (e) => simNao(e['ativo']), classe: ativoBadge },
  ],
  campos: [
    { nome: 'nome', rotulo: 'Nome', tipo: 'texto', obrigatorio: true, inteira: true },
    { nome: 'login', rotulo: 'Login', tipo: 'texto', obrigatorio: true,
      validar: (v) => (/^[a-zA-Z0-9._-]{3,30}$/.test(v ?? '') ? null : '3 a 30 letras, numeros, ponto, _ ou -') },
    { nome: 'senha', rotulo: 'Senha', tipo: 'senha', dica: 'Na alteracao, deixe em branco para manter a atual',
      validar: (v, _m, editando) => (!v && editando ? null : !v || v.length < 6 ? 'Minimo de 6 caracteres' : null) },
    { nome: 'perfil', rotulo: 'Perfil', tipo: 'select', obrigatorio: true,
      opcoes: [{ valor: 'VENDEDOR', rotulo: 'Vendedor' }, { valor: 'ADMIN', rotulo: 'Administrador' }] },
    { nome: 'ativo', rotulo: 'Ativo (pode entrar no sistema)', tipo: 'check' },
  ],
};
