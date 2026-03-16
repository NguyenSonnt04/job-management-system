import re
with open('src/main/resources/static/cv-editor.html', 'r', encoding='utf-8') as f:
    text = f.read()

# Replace any escaped single quotes in getItemControls call
# e.g. getItemControls(\'experience\', idx) -> getItemControls('experience', idx)
text = text.replace(r"\'", "'")

with open('src/main/resources/static/cv-editor.html', 'w', encoding='utf-8') as f:
    f.write(text)
