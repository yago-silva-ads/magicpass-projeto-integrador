/** Mesmas regras de digito verificador do backend, para feedback imediato no formulario. */

const soDigitos = (v: string | null | undefined) => (v ?? '').replace(/\D/g, '');
const todosIguais = (s: string) => s.length > 0 && s.split('').every((c) => c === s[0]);

export function cpfValido(valor: string): boolean {
  const cpf = soDigitos(valor);
  if (cpf.length !== 11 || todosIguais(cpf)) return false;
  const d = cpf.split('').map(Number);
  const dv = (qtd: number) => {
    let soma = 0;
    for (let i = 0; i < qtd; i++) soma += d[i] * (qtd + 1 - i);
    const r = (soma * 10) % 11;
    return r === 10 ? 0 : r;
  };
  return d[9] === dv(9) && d[10] === dv(10);
}

export function cnpjValido(valor: string): boolean {
  const cnpj = (valor ?? '').toUpperCase().replace(/[^0-9A-Z]/g, '');
  if (!/^[0-9A-Z]{12}\d{2}$/.test(cnpj) || todosIguais(cnpj)) return false;
  const v = cnpj.split('').map((c) => c.charCodeAt(0) - 48);
  const mod11 = (pesos: number[]) => {
    const soma = pesos.reduce((s, p, i) => s + v[i] * p, 0);
    const r = soma % 11;
    return r < 2 ? 0 : 11 - r;
  };
  return v[12] === mod11([5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]) && v[13] === mod11([6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
}

export function rgValido(valor: string): boolean {
  const rg = (valor ?? '').toUpperCase().replace(/[^0-9X]/g, '');
  if (!/^\d{8}[0-9X]$/.test(rg) || todosIguais(rg.slice(0, 8))) return false;
  let soma = 0;
  for (let i = 0; i < 8; i++) soma += Number(rg[i]) * (i + 2);
  const dv = 11 - (soma % 11);
  const esperado = dv === 10 ? 'X' : dv === 11 ? '0' : String(dv);
  return rg[8] === esperado;
}

export function ieValida(valor: string, uf: string): boolean {
  if ((valor ?? '').trim().toUpperCase() === 'ISENTO') return true;
  const ie = soDigitos(valor);
  if (!ie || todosIguais(ie)) return false;
  const d = ie.split('').map(Number);
  const mod11 = (pesos: number[]) => {
    const r = pesos.reduce((s, p, i) => s + d[i] * p, 0) % 11;
    return r < 2 ? 0 : 11 - r;
  };
  switch ((uf ?? '').toUpperCase()) {
    case 'SP': {
      if (d.length !== 12) return false;
      const s1 = [1, 3, 4, 5, 6, 7, 8, 10].reduce((s, p, i) => s + d[i] * p, 0);
      const s2 = [3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2].reduce((s, p, i) => s + d[i] * p, 0);
      return d[8] === (s1 % 11) % 10 && d[11] === (s2 % 11) % 10;
    }
    case 'RJ':
      return d.length === 8 && d[7] === mod11([2, 7, 6, 5, 4, 3, 2]);
    case 'PR':
      return d.length === 10 && d[8] === mod11([3, 2, 7, 6, 5, 4, 3, 2]) && d[9] === mod11([4, 3, 2, 7, 6, 5, 4, 3, 2]);
    case 'SC':
      return d.length === 9 && d[8] === mod11([9, 8, 7, 6, 5, 4, 3, 2]);
    default:
      return false;
  }
}

// ------------------------------------------------------------- mascaras de exibicao
export function mascaraCpf(v: string): string {
  const d = soDigitos(v);
  return d.length === 11 ? d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4') : v ?? '';
}

export function mascaraCnpj(v: string): string {
  const c = (v ?? '').toUpperCase().replace(/[^0-9A-Z]/g, '');
  return c.length === 14 ? `${c.slice(0, 2)}.${c.slice(2, 5)}.${c.slice(5, 8)}/${c.slice(8, 12)}-${c.slice(12)}` : v ?? '';
}

export function mascaraRg(v: string): string {
  const r = (v ?? '').toUpperCase().replace(/[^0-9X]/g, '');
  return r.length === 9 ? `${r.slice(0, 2)}.${r.slice(2, 5)}.${r.slice(5, 8)}-${r.slice(8)}` : v ?? '';
}

export function mascaraTelefone(v: string): string {
  const d = soDigitos(v);
  if (d.length === 11) return d.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  if (d.length === 10) return d.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  return v ?? '';
}
