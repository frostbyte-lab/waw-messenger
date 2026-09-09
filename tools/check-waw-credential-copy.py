from pathlib import Path
for path in [
    Path('/home/ubuntu/waw-messenger/app/src/main/assets/waw-dashboard.html'),
    Path('/home/ubuntu/waw-messenger/app/src/main/java/com/waw/messenger/LoginActivity.kt'),
]:
    text = path.read_text(errors='replace').lower()
    print(path)
    for term in ['username / email', 'email', 'password', 'lupa password', 'masuk ke waw']:
        print(f'  {term}: {text.count(term)}')
