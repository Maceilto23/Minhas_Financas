
const $=s=>document.querySelector(s);
const money=v=>Number(v||0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'});
const key='meucaixa_entries_v1';
const uid=()=>crypto.randomUUID?crypto.randomUUID():String(Date.now()+Math.random());

let entries=JSON.parse(localStorage.getItem(key)||'null');
if(!Array.isArray(entries)){
  entries=[
    {id:uid(),type:'income',description:'Salário',category:'Salário',amount:3000,dueDate:'2026-10-07',status:'pending',recurrence:'monthly',notes:'Valor aproximado'},
    {id:uid(),type:'income',description:'Visão Ferragens',category:'Sistemas',amount:350,dueDate:'2026-09-20',status:'pending',recurrence:'monthly',notes:''},
    {id:uid(),type:'income',description:'Casa do Criador',category:'Sistemas',amount:160,dueDate:'2026-09-26',status:'pending',recurrence:'monthly',notes:''},
    {id:uid(),type:'income',description:'RFK',category:'Sistemas',amount:120,dueDate:'2026-09-30',status:'pending',recurrence:'monthly',notes:''},
    {id:uid(),type:'income',description:'Peixaria',category:'Sistemas',amount:380,dueDate:'2026-09-30',status:'pending',recurrence:'monthly',notes:''},
    {id:uid(),type:'expense',description:'Velo',category:'Sistemas',amount:500,dueDate:'2026-09-30',status:'pending',recurrence:'monthly',notes:''},
    {id:uid(),type:'expense',description:'Cartão Banco do Brasil',category:'Cartão',amount:963.06,dueDate:'2026-09-20',status:'pending',recurrence:'none',notes:''},
    {id:uid(),type:'expense',description:'Pagamento da casa',category:'Casa',amount:1600,dueDate:'2026-09-28',status:'pending',recurrence:'none',notes:''},
    {id:uid(),type:'expense',description:'Aluguel',category:'Casa',amount:400,dueDate:'2026-09-10',status:'paid',recurrence:'monthly',notes:'Setembro já pago'},
    {id:uid(),type:'expense',description:'Internet',category:'Casa',amount:120,dueDate:'2026-09-10',status:'paid',recurrence:'monthly',notes:'Setembro já pago'},
    {id:uid(),type:'expense',description:'Claro móvel',category:'Telefone',amount:60,dueDate:'2026-09-15',status:'pending',recurrence:'monthly',notes:''},
    {id:uid(),type:'expense',description:'Amazon Prime',category:'Assinaturas',amount:40,dueDate:'2026-09-15',status:'pending',recurrence:'monthly',notes:''},
    {id:uid(),type:'expense',description:'Compras do mês',category:'Alimentação',amount:500,dueDate:'2026-09-30',status:'pending',recurrence:'monthly',notes:''}
  ];
  save();
}

function save(){localStorage.setItem(key,JSON.stringify(entries))}
function selectedMonth(){return $('#monthPicker').value}
function monthEntries(){
  let m=selectedMonth();
  let exact=entries.filter(e=>e.dueDate.startsWith(m));
  // project recurring items into selected month if no exact clone exists
  const [y,mo]=m.split('-').map(Number);
  entries.filter(e=>e.recurrence==='monthly').forEach(e=>{
    const d=Number(e.dueDate.slice(8,10));
    const day=Math.min(d,new Date(y,mo,0).getDate());
    const date=`${y}-${String(mo).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
    const exists=exact.some(x=>x.description===e.description && x.dueDate===date);
    if(!exists) exact.push({...e,id:'virtual:'+e.id+':'+m,dueDate:date,status:'pending',virtual:true});
  });
  return exact;
}
function render(){
  const type=$('#typeFilter').value,status=$('#statusFilter').value,q=$('#search').value.toLowerCase();
  let all=monthEntries().sort((a,b)=>a.dueDate.localeCompare(b.dueDate));
  let filtered=all.filter(e=>(!type||e.type===type)&&(!status||e.status===status)&&(!q||(e.description+' '+e.category).toLowerCase().includes(q)));
  $('#list').innerHTML=filtered.length?filtered.map(e=>`<div class="row ${e.type}" data-id="${e.id}">
    <div><strong>${e.description}</strong><span class="badge ${e.status}">${e.status==='paid'?'OK':'Pendente'}</span>
    <div class="meta">${new Date(e.dueDate+'T12:00:00').toLocaleDateString('pt-BR')} • ${e.category}${e.recurrence==='monthly'?' • mensal':''}</div></div>
    <div class="amt">${e.type==='income'?'+':'−'} ${money(e.amount)}</div></div>`).join(''):'<p class="muted">Nenhum lançamento neste filtro.</p>';
  const inc=all.filter(e=>e.type==='income').reduce((s,e)=>s+Number(e.amount),0);
  const exp=all.filter(e=>e.type==='expense').reduce((s,e)=>s+Number(e.amount),0);
  const cleared=all.filter(e=>e.status==='paid').reduce((s,e)=>s+(e.type==='income'?1:-1)*Number(e.amount),0);
  $('#kpiIncome').textContent=money(inc); $('#kpiExpense').textContent=money(exp); $('#kpiBalance').textContent=money(inc-exp); $('#kpiCleared').textContent=money(cleared);
  renderChecklist(all); renderCats(all); calculateGoal();
  document.querySelectorAll('.row').forEach(r=>r.onclick=()=>openEntry(r.dataset.id));
}
function renderChecklist(all){
  const pending=all.filter(e=>e.status==='pending').sort((a,b)=>a.dueDate.localeCompare(b.dueDate));
  const overdue=pending.filter(e=>new Date(e.dueDate+'T23:59:59')<new Date());
  let html='';
  if(overdue.length) html+=`<div class="check-item"><strong>⚠ ${overdue.length} vencido(s)</strong><small>Revise imediatamente.</small></div>`;
  const next=pending.slice(0,5);
  html+=next.map(e=>`<div class="check-item"><strong>${e.type==='expense'?'Pagar':'Receber'} ${money(e.amount)}</strong><small>${e.description} • ${new Date(e.dueDate+'T12:00').toLocaleDateString('pt-BR')}</small></div>`).join('');
  if(!pending.length) html='<div class="check-item"><strong>✓ Tudo conferido</strong><small>Não há pendências neste mês.</small></div>';
  $('#checklist').innerHTML=html;
}
function renderCats(all){
  const map={};
  all.forEach(e=>{if(e.type==='expense')map[e.category]=(map[e.category]||0)+Number(e.amount)});
  $('#categorySummary').innerHTML=Object.entries(map).sort((a,b)=>b[1]-a[1]).map(([k,v])=>`<div class="catline"><span>${k}</span><strong>${money(v)}</strong></div>`).join('')||'<p class="muted">Sem despesas.</p>';
}
function openEntry(id=null){
  let e=entries.find(x=>x.id===id);
  if(String(id||'').startsWith('virtual:')){
    const base=id.split(':')[1], src=entries.find(x=>x.id===base);
    if(src){ e={...src,id:'',dueDate:monthEntries().find(x=>x.id===id).dueDate,status:'pending'}; }
  }
  $('#modalTitle').textContent=e?'Editar lançamento':'Novo lançamento';
  $('#entryId').value=e?.id||''; $('#type').value=e?.type||'expense'; $('#description').value=e?.description||''; $('#category').value=e?.category||''; $('#amount').value=e?.amount||''; $('#dueDate').value=e?.dueDate||`${selectedMonth()}-01`; $('#status').value=e?.status||'pending'; $('#recurrence').value=e?.recurrence||'none'; $('#notes').value=e?.notes||''; $('#deleteBtn').hidden=!e?.id; $('#modal').showModal();
}
$('#entryForm').addEventListener('submit',ev=>{
  ev.preventDefault();
  const obj={id:$('#entryId').value||uid(),type:$('#type').value,description:$('#description').value.trim(),category:$('#category').value.trim(),amount:Number($('#amount').value),dueDate:$('#dueDate').value,status:$('#status').value,recurrence:$('#recurrence').value,notes:$('#notes').value.trim()};
  const i=entries.findIndex(e=>e.id===obj.id); if(i>=0)entries[i]=obj; else entries.push(obj); save(); $('#modal').close(); render();
});
$('#deleteBtn').onclick=()=>{const id=$('#entryId').value;if(id&&confirm('Excluir este lançamento?')){entries=entries.filter(e=>e.id!==id);save();$('#modal').close();render();}};
$('#newBtn').onclick=()=>openEntry(); $('#closeModal').onclick=()=>$('#modal').close();
['typeFilter','statusFilter','search'].forEach(id=>$('#'+id).addEventListener('input',render));
function shiftMonth(delta){let [y,m]=selectedMonth().split('-').map(Number);m+=delta;if(m<1){m=12;y--}if(m>12){m=1;y++}$('#monthPicker').value=`${y}-${String(m).padStart(2,'0')}`;render()}
$('#prevMonth').onclick=()=>shiftMonth(-1); $('#nextMonth').onclick=()=>shiftMonth(1); $('#refreshBtn').onclick=render; $('#monthPicker').onchange=render;
function calculateGoal(){
  const all=monthEntries(), balance=all.reduce((s,e)=>s+(e.type==='income'?1:-1)*Number(e.amount),0), reserve=Number($('#reserveInput').value||0), goal=Number($('#goalInput').value||0), available=Math.max(0,balance-reserve);
  $('#goalResult').innerHTML=`Saldo previsto do mês: <strong>${money(balance)}</strong><br>Após reserva mínima: <strong>${money(available)}</strong><br>${available>=goal?'✅ Meta da obra cabe no mês.':`Faltam <strong>${money(Math.max(0,goal-available))}</strong> para a meta de ${money(goal)}.`}`;
}
$('#calcGoal').onclick=calculateGoal; $('#reserveInput').oninput=calculateGoal; $('#goalInput').oninput=calculateGoal;
$('#exportBtn').onclick=()=>{const blob=new Blob([JSON.stringify({version:1,entries},null,2)],{type:'application/json'});const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='meucaixa-backup.json';a.click();URL.revokeObjectURL(a.href)};
$('#importFile').onchange=async e=>{const f=e.target.files[0];if(!f)return;try{const data=JSON.parse(await f.text());if(!Array.isArray(data.entries))throw 0;if(confirm('Substituir os lançamentos atuais pelo backup?')){entries=data.entries;save();render()}}catch{alert('Arquivo de backup inválido.')}e.target.value=''};
const now=new Date(); $('#monthPicker').value=`${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}`; render();

let deferredPrompt=null;
window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();deferredPrompt=e;$('#installBtn').hidden=false});
$('#installBtn').onclick=async()=>{if(deferredPrompt){deferredPrompt.prompt();await deferredPrompt.userChoice;deferredPrompt=null;$('#installBtn').hidden=true}};
if('serviceWorker' in navigator) navigator.serviceWorker.register('sw.js');
