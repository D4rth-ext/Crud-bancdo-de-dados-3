const $ = s => document.querySelector(s);
const types = ['Bug','Dark','Dragon','Electric','Fairy','Fighting','Fire','Flying','Ghost','Grass','Ground','Ice','Normal','Poison','Psychic','Rock','Steel','Water'];
const colors = {Grass:'#67a45b',Fire:'#e87542',Water:'#548ccc',Electric:'#b89c26',Poison:'#a06db8',Bug:'#91a648',Psychic:'#d175a3',Ice:'#6cacae',Dragon:'#7563bb'};
for (const select of [$('#filters [name=type]'),$('[name=type1]'),$('[name=type2]')]) for(const type of types) select.add(new Option(type,type));
for(let i=1;i<=9;i++) $('#filters [name=generation]').add(new Option(`Geração ${i}`,i));
let page=0,total=0,selected=null,editing=false,request=0;
const escape = value => String(value).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const number = id => `#${String(id).padStart(3,'0')}`;
const badges = p => p.types.map(t=>`<span class="badge">${escape(t)}</span>`).join('');
async function api(path='',options={}) {
  const response=await fetch('/api/pokemons'+path,{...options,headers:{'Content-Type':'application/json',...options.headers}});
  if(!response.ok){const error=await response.json().catch(()=>({}));throw new Error(error.message||'Não foi possível concluir a operação.');}
  return response.status===204?null:response.json();
}
async function load() {
  const current=++request; $('#message').textContent='Consultando a Pokédex…';
  const query=new URLSearchParams();for(const [k,v] of new FormData($('#filters')))if(v)query.set(k,v);
  query.set('page',page);query.set('size',24);
  try {
    const data=await api('?'+query);if(current!==request)return;
    total=data.total;
    if(page>0&&page*24>=total){page=Math.max(0,Math.ceil(total/24)-1);return load();}
    $('#count').textContent=total;$('#message').textContent=total?'':'Nenhum Pokémon encontrado. Tente outros filtros ou cadastre uma nova espécie.';
    $('#cards').innerHTML=data.items.map(p=>`<button class="card" data-id="${p._id}" aria-label="Consultar ${escape(p.name)}"><span class="number">${number(p._id)}</span><span class="orb" style="--tint:${colors[p.types[0]]||'#718192'}18;--accent:${colors[p.types[0]]||'#718192'}">◈</span><h3>${escape(p.name)}</h3>${badges(p)}<div class="card-meta"><span>Geração ${p.generation}</span><span class="${p.legendary?'legend':''}">${p.legendary?'✦ Lendário':'Ver ficha →'}</span></div></button>`).join('');
    $('#page').textContent=`Página ${page+1} de ${Math.max(1,Math.ceil(total/24))}`;$('#prev').disabled=page===0;$('#next').disabled=(page+1)*24>=total;
  }catch(e){if(current!==request)return;$('#cards').replaceChildren();$('#message').textContent=e.message;$('#prev').disabled=true;$('#next').disabled=true;$('#count').textContent='—';$('#page').textContent='';}
}
$('#filters').addEventListener('submit',e=>{e.preventDefault();page=0;load();});
$('#filters').addEventListener('reset',()=>setTimeout(()=>{page=0;load();},0));
$('#prev').onclick=()=>{page--;load();};$('#next').onclick=()=>{page++;load();};
document.querySelectorAll('[data-close]').forEach(b=>b.onclick=()=>$('#'+b.dataset.close).close());
$('#cards').onclick=async e=>{
 const card=e.target.closest('[data-id]');if(!card)return;
 try {selected=await api('/'+card.dataset.id);const p=selected;
 $('#detail-content').innerHTML=`<span class="eyebrow">${number(p._id)} · GERAÇÃO ${p.generation}</span><h2>${escape(p.name)}</h2>${badges(p)}<p>${p.legendary?'✦ Pokémon lendário':'Ficha de atributos'}</p>`+[['hp','HP'],['attack','Ataque'],['defense','Defesa'],['speed','Velocidade']].map(([key,label])=>`<div class="stat"><div class="stat-label"><span>${label}</span><strong>${p[key]}</strong></div><div class="bar"><i style="width:${p[key]/255*100}%"></i></div></div>`).join('');
 $('#details').showModal();}catch(e){$('#message').textContent=e.message;}
};
function openEditor(p=null){editing=!!p;const f=$('#pokemon-form');f.reset();$('#form-error').textContent='';$('#editor-title').textContent=p?'Editar Pokémon':'Novo Pokémon';f.elements._id.readOnly=!!p;
 if(p){for(const key of ['_id','name','generation','hp','attack','defense','speed'])f.elements[key].value=p[key];f.elements.legendary.checked=p.legendary;f.elements.type1.value=p.types[0];f.elements.type2.value=p.types[1]||'';}
 $('#editor').showModal();}
$('#create').onclick=()=>openEditor();$('#edit').onclick=()=>{$('#details').close();openEditor(selected);};
$('#pokemon-form').onsubmit=async e=>{e.preventDefault();const f=e.target;const data=Object.fromEntries(new FormData(f));const pokemon={};
 for(const key of ['_id','generation','hp','attack','defense','speed'])pokemon[key]=Number(data[key]);
 pokemon.name=data.name.trim();pokemon.types=[data.type1,data.type2].filter(Boolean);pokemon.legendary=f.elements.legendary.checked;
 if(!pokemon.name){$('#form-error').textContent='Informe o nome do Pokémon.';return;}
 $('#save').disabled=true;
 try{await api(editing?'/'+pokemon._id:'',{method:editing?'PUT':'POST',body:JSON.stringify(pokemon)});$('#editor').close();await load();}
 catch(e){$('#form-error').textContent=e.message;}finally{$('#save').disabled=false;}
};
$('#delete').onclick=async()=>{if(!selected||!confirm(`Excluir ${selected.name} (${number(selected._id)})? Essa ação não pode ser desfeita.`))return;
 $('#delete').disabled=true;try{await api('/'+selected._id,{method:'DELETE'});$('#details').close();await load();}catch(e){alert(e.message);}finally{$('#delete').disabled=false;}
};
load();
