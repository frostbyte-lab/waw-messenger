from pathlib import Path

path = Path('/home/ubuntu/waw-messenger/app/src/main/assets/waw-dashboard.html')
text = path.read_text()
replacements = {
    'Login dengan Google': 'Buka WhatsApp Web resmi',
    'Login aman dengan verifikasi 2 langkah, enkripsi password, dan session token seperti WhatsApp asli. Tidak simpan password plain text.': 'Hubungkan melalui QR/linking resmi WhatsApp Web. WAW tidak meminta atau menyimpan email, password, cookie, atau session WhatsApp.',
    'Autentikasi & Enkripsi': 'WhatsApp Web resmi & aman',
    'Login di Web, Desktop & Mobile sekaligus': 'Hubungkan melalui WhatsApp Web resmi',
}
for old, new in replacements.items():
    text = text.replace(old, new)
path.write_text(text)
print('updated', path)
for old in replacements:
    print(old, 'remaining=', text.count(old))
