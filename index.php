<?php
declare(strict_types=1);
?><!doctype html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
  <meta name="theme-color" content="#111827">
  <title>MeuCaixa • Controle Financeiro</title>
  <link rel="manifest" href="manifest.webmanifest">
  <link rel="stylesheet" href="assets/style.css">
</head>
<body>
<header class="topbar">
  <div>
    <div class="brand">MeuCaixa</div>
    <div class="sub">Seu mês inteiro, sem esquecer nada.</div>
  </div>
  <button id="installBtn" class="ghost" hidden>Instalar app</button>
</header>

<main class="wrap">
  <section class="toolbar card">
    <button id="prevMonth">◀</button>
    <input id="monthPicker" type="month">
    <button id="nextMonth">▶</button>
    <button id="refreshBtn" class="ghost">Atualizar</button>
  </section>

  <section class="grid kpis">
    <div class="card kpi"><span>Entradas</span><strong id="kpiIncome">R$ 0,00</strong></div>
    <div class="card kpi"><span>Saídas</span><strong id="kpiExpense">R$ 0,00</strong></div>
    <div class="card kpi"><span>Saldo previsto</span><strong id="kpiBalance">R$ 0,00</strong></div>
    <div class="card kpi"><span>Pago/recebido</span><strong id="kpiCleared">R$ 0,00</strong></div>
  </section>

  <section class="grid">
    <div class="card">
      <div class="section-title"><h2>Lançamentos do mês</h2><button id="newBtn">+ Novo</button></div>
      <div class="filters">
        <select id="typeFilter"><option value="">Todos</option><option value="income">Entradas</option><option value="expense">Saídas</option></select>
        <select id="statusFilter"><option value="">Todos os status</option><option value="pending">Pendentes</option><option value="paid">Pagos/recebidos</option></select>
        <input id="search" placeholder="Buscar lançamento...">
      </div>
      <div id="list" class="list"></div>
    </div>

    <aside class="card">
      <div class="section-title"><h2>Checklist inteligente</h2></div>
      <div id="checklist" class="checklist"></div>
      <hr>
      <div class="section-title"><h2>Por categoria</h2></div>
      <div id="categorySummary"></div>
    </aside>
  </section>

  <section class="card">
    <div class="section-title"><h2>Reserva e obra</h2></div>
    <div class="form-grid">
      <label>Reserva mínima desejada
        <input id="reserveInput" type="number" step="0.01" min="0" value="1500">
      </label>
      <label>Meta da obra/material
        <input id="goalInput" type="number" step="0.01" min="0" value="6616">
      </label>
      <button id="calcGoal" class="primary">Calcular disponibilidade</button>
    </div>
    <div id="goalResult" class="goal-result"></div>
  </section>

  <section class="card">
    <div class="section-title"><h2>Backup</h2></div>
    <p class="muted">Exporte seus lançamentos regularmente. Assim você não perde seu histórico.</p>
    <div class="actions">
      <button id="exportBtn" class="ghost">Exportar JSON</button>
      <label class="ghost file-label">Importar JSON<input id="importFile" type="file" accept=".json,application/json"></label>
    </div>
  </section>
</main>

<dialog id="modal">
  <form id="entryForm" method="dialog">
    <div class="section-title"><h2 id="modalTitle">Novo lançamento</h2><button type="button" id="closeModal" class="ghost">✕</button></div>
    <input id="entryId" type="hidden">
    <div class="form-grid">
      <label>Tipo
        <select id="type" required><option value="expense">Despesa</option><option value="income">Receita</option></select>
      </label>
      <label>Descrição<input id="description" required maxlength="120"></label>
      <label>Categoria<input id="category" required maxlength="60" placeholder="Ex.: Casa, Alimentação, Sistema"></label>
      <label>Valor<input id="amount" type="number" step="0.01" min="0.01" required></label>
      <label>Vencimento/recebimento<input id="dueDate" type="date" required></label>
      <label>Status
        <select id="status"><option value="pending">Pendente</option><option value="paid">Pago/recebido</option></select>
      </label>
      <label>Recorrência
        <select id="recurrence"><option value="none">Não repetir</option><option value="monthly">Mensal</option></select>
      </label>
      <label>Observação<textarea id="notes" rows="3" maxlength="300"></textarea></label>
    </div>
    <div class="actions end">
      <button type="button" id="deleteBtn" class="danger" hidden>Excluir</button>
      <button type="submit" class="primary">Salvar</button>
    </div>
  </form>
</dialog>

<script src="assets/app.js"></script>
</body>
</html>
