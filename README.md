# Minhas Finanças — Android + SQLite + Google Drive

Aplicativo Android offline para controle financeiro pessoal.

## Arquitetura
- Android nativo em Kotlin
- SQLite local via Room
- Funciona sem internet
- Receitas, despesas, status, categorias e recorrências mensais
- Reserva mínima e meta da obra
- Backup do arquivo SQLite em uma pasta escolhida pelo usuário
- Compatível com uma pasta do Google Drive pelo seletor de documentos do Android
- Backup automático via WorkManager a cada 12 horas quando há rede
- Restauração do SQLite pelo próprio aplicativo
- GitHub Actions pronto para gerar APK

## Como a sincronização funciona
O aplicativo NÃO grava senha do Gmail.

1. No celular, toque em **Escolher Drive**.
2. O Android abre o seletor de arquivos.
3. Escolha uma pasta dentro do seu Google Drive.
4. O aplicativo recebe permissão persistente para essa pasta.
5. O arquivo `Minhas_Financas_Backup.sqlite` será atualizado nessa pasta.

Assim o banco continua local e offline, mas o backup fica sincronizado pela conta Google configurada no aparelho.

## GitHub
Suba TODO o conteúdo deste pacote na raiz do repositório.

O arquivo:
`.github/workflows/main.yml`

compila automaticamente o APK em cada push na branch `main`.

Depois:
**GitHub > Actions > Compilar Minhas Financas APK > último build > Artifacts > Minhas-Financas-APK**

## Dados iniciais
A primeira execução já traz como exemplo/base os valores informados:
- saldo inicial R$ 4.221,20
- Visão Ferragens R$ 350/mês
- Casa do Criador R$ 160/mês
- RFK R$ 120/mês
- Peixaria R$ 380/mês
- Velo R$ 500/mês
- cartão BB R$ 963,06
- casa R$ 1.600,00
- aluguel R$ 400/mês, setembro pago
- internet R$ 120/mês, setembro pago
- Claro R$ 60/mês
- compras R$ 500/mês
- salário aproximado R$ 3.000/mês a partir de 07/10/2026
- reserva padrão R$ 1.500
- meta da obra R$ 6.616

Tudo pode ser editado/excluído pelo aplicativo.

## Observação importante
Essa versão sincroniza o banco por backup/restore do arquivo SQLite. Ela não faz mesclagem simultânea de lançamentos entre dois celulares diferentes. Para uso pessoal em um aparelho com cópia no Drive, é o modelo mais simples e seguro sem servidor.
