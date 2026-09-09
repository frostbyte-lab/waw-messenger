from pathlib import Path

path = Path('/home/ubuntu/waw-messenger/app/src/main/assets/waw-dashboard.html')
text = path.read_text()
old = 'K("button",{className:"w-full h-[48px] rounded-2xl bg-white border border-zinc-200 font-semibold text-[13.5px] flex items-center justify-center gap-2",children:[s("div",{className:"w-5 h-5 rounded-full bg-white border flex items-center justify-center text-[12px] font-bold",children:"G"})," Buka WhatsApp Web resmi"]})'
new = 'K("button",{onClick:()=>{window.location.href="https://web.whatsapp.com"},className:"w-full h-[48px] rounded-2xl bg-white border border-zinc-200 font-semibold text-[13.5px] flex items-center justify-center gap-2",children:[s("div",{className:"w-5 h-5 rounded-full bg-[#12c785] text-white flex items-center justify-center text-[9px] font-bold",children:"WA"})," Buka WhatsApp Web resmi"]})'
if old not in text:
    raise SystemExit('target login button not found')
path.write_text(text.replace(old, new, 1))
print('wired official WhatsApp Web button')
