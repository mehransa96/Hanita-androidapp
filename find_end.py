with open("app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt", "r") as f:
    lines = f.readlines()

brace_count = 0
in_class = False
for i, line in enumerate(lines):
    if "class MahitaViewModel" in line:
        in_class = True
    if in_class:
        brace_count += line.count("{")
        brace_count -= line.count("}")
        if brace_count == 0:
            print(f"Class ends at line {i+1}")
            break
