from pathlib import Path

path = Path('/home/ubuntu/waw-messenger/app/src/main/assets/waw-dashboard.html')
text = path.read_text()
start_marker = 'K("div",{className:"mt-8 space-y-4",children:['
end_marker = ',K("div",{className:"mt-8 rounded-2xl bg-[#e9f8ef]'
start = text.find(start_marker)
end = text.find(end_marker, start)
if start < 0 or end < 0:
    raise SystemExit(f'credential block not found start={start} end={end}')
replacement = '''K("div",{className:"mt-8 space-y-4",children:[K("div",{className:"rounded-2xl bg-white border border-zinc-200 p-4",children:[K("div",{className:"flex items-center gap-3",children:[s("div",{className:"w-10 h-10 rounded-full bg-[#12c785] text-white flex items-center justify-center font-bold text-[10px]",children:"WA"}),K("div",{children:[s("div",{className:"font-bold text-[14px]",children:"WhatsApp Web resmi"}),s("div",{className:"text-[11.5px] text-zinc-500 mt-0.5",children:"Hubungkan dengan QR atau linking resmi WhatsApp"})]})]}),s("div",{className:"mt-3 text-[12px] text-zinc-600 leading-[1.4]",children:"WAW tidak meminta email atau password WhatsApp. Proses login dilakukan langsung oleh WhatsApp Web resmi."})]}),K("button",{onClick:()=>{window.location.href="https://web.whatsapp.com"},className:"w-full h-[50px] rounded-2xl bg-black text-white font-bold text-[14px] flex items-center justify-center gap-2 active:scale-[0.98] transition",children:[s(Vr,{className:"w-4 h-4"})," Buka WhatsApp Web resmi"]})]})'''
path.write_text(text[:start] + replacement + text[end:])
print('replaced credential form with official WhatsApp Web linking card')
