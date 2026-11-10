# ✨ MagicPass Viagens — Projeto Integrador SENAC 2026

Sistema de **venda de ingressos e pacotes de viagem para os parques Disney**, com campanha **Black Friday**.
Back end em **Spring Boot 3.5 (Java 17+)** e front end em **Angular 21**, seguindo POO e MVC.

## Como rodar

### Opção A — um único arquivo (recomendado para a máquina do professor)
Requisito: Java 17 ou mais novo.
```
java -jar magicpass.jar
```
Abra **http://localhost:8080**. O banco H2 é criado sozinho na pasta `data/` e já vem com dados de exemplo.
(Para gerar o `magicpass.jar` e o `.zip`: `powershell -ExecutionPolicy Bypass -File empacotar.ps1`)

### Opção B — desenvolvimento (2 terminais)
```
cd backend   &&  mvn spring-boot:run          # API em http://localhost:8080
cd frontend  &&  npm install && npx ng serve  # tela em http://localhost:4200
```

### Logins iniciais
| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Vendedor | `vendedor` | `vendedor123` |

Novos usuários são cadastrados pelo administrador na tela **Usuários** (as senhas são gravadas com hash BCrypt).

## Requisitos do PI atendidos
| Requisito | Onde |
|---|---|
| Spring Boot + Angular | `backend/` e `frontend/` |
| CRUD completo (cadastrar, ler, alterar, apagar) | Clientes, Parceiros, Parques, Ingressos/Pacotes, Promoções, Usuários e Vendas |
| POO + MVC | Model (`model/`, herança de `EntidadeBase`), Controller (`CrudController` genérico), Service (`CrudService` com Template Method) e View (Angular) |
| Login e senha cadastrados pelo administrador | Tela Usuários (só ADMIN), token de sessão, perfis ADMIN/VENDEDOR |
| CPF, CNPJ, RG e IE validados pelos dígitos verificadores | `validation/DocumentoValidator.java` (back) e `core/validadores.ts` (front). CNPJ aceita o novo formato alfanumérico; IE para SP, RJ, PR e SC (ou ISENTO) |

## Regras que evitam erros (ex.: "-1")
- Id nulo, zero, negativo ou inexistente → **404 com mensagem clara**, nunca exceção crua.
- Preço, estoque, dias, quantidade e desconto negativos são recusados (back e front).
- O estoque **nunca fica negativo**: venda, alteração, cancelamento e exclusão ajustam o estoque numa transação (tudo ou nada).
- Não deixa apagar registro em uso (cliente com venda, produto vendido, parque com produtos) → 409 com orientação.
- Login duplicado, CPF/CNPJ duplicado e cupom duplicado são bloqueados.
- O último administrador ativo não pode ser excluído nem desativado.
- Todo erro volta como JSON `{status, mensagem, campos}` e aparece no formulário, ao lado do campo.

## Black Friday (motor de preços)
- Campanha automática por categoria (Ingressos 30% / Pacotes 40%).
- **Família/grupo:** a partir de 4 itens, +5%.
- **Cupom** `BLACKMAGIC`: +10%.
- **PIX:** +5% no total; cartão em até 12x sem juros.
- Desconto máximo de 70% por item. A vitrine pública `/black-friday` tem contagem regressiva, cupom que copia com um clique e um simulador para a viagem da família.

## Visual e animacoes
- Fotos reais dos 6 parques e dos personagens nos desfiles (Mickey, Cinderela, Rapunzel, Dumbo, Ariel), todas do Wikimedia Commons com licenca livre. Os creditos ficam em `frontend/public/img/CREDITOS.md` e no rodape da vitrine.
- Icones SVG proprios no lugar dos emojis; o logo e uma estrela dourada animada.
- Animacoes: zoom lento nas fotos de fundo, brilhos dourados subindo, contagem regressiva pulsando, cards que aparecem em cascata ao rolar a pagina, faixa de personagens rolando sem parar, numeros do painel contando ate o valor e transicao suave entre as telas.
- Quem ativa "reduzir movimento" no sistema operacional ve tudo sem animacao.

## Testes automatizados
```
cd backend && mvn test
```
São 12 testes: validadores e fluxo real da API, cobrindo CRUD, ids -1/0, negativos, estoque, permissões e Black Friday.

## Próximos passos (escalar para venda real)
- Tornar-se revendedor autorizado ou integrar com uma operadora/consolidadora credenciada (a venda de ingressos oficiais exige esse vínculo).
- Gateway de pagamento (PIX/cartão) e emissão de voucher.
- Carrinho público para o cliente final, migração para MySQL/PostgreSQL (perfil `mysql` já incluído) e JWT.

> Projeto acadêmico. Disney e as marcas relacionadas pertencem à The Walt Disney Company. Os preços são ilustrativos.
