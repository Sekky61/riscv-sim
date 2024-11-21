import json
from collections import defaultdict
import sys

# See https://github.com/riscv/riscv-opcodes/blob/master/README.md
# For the input

# Load the JSON
json_data = sys.stdin.read()

# Parse the JSON
instructions = json.loads(json_data)

# Group instructions by mask
mask_groups = {}
for name, data in instructions.items():
    mask = data["mask"]
    if mask not in mask_groups:
        mask_groups[mask] = []
    mask_groups[mask].append({"match": data["match"], "instruction": name})

# sort by match inside each mask group
for mask, entries in mask_groups.items():
    # convert hex strings to integers
    mask_groups[mask] = sorted(entries, key=lambda x: int(x["match"], 16))

def sanitize(name):
    if name == "break" or name == "or" or name == "and":
        return f"@\"{name}\""
    return name

# Generate Zig code
zig_code = "const encodings: []const DecodingTable([]const InstructionEncoding) = &.{\n"
for mask, entries in mask_groups.items():
    zig_code += f"    .{{\n"
    zig_code += f"        .mask = {mask},\n"
    zig_code += "        .table = &.{\n"
    for entry in entries:
        instr_sanitized = sanitize(entry["instruction"])
        zig_code += f"            .{{ .match = {entry['match']}, .instruction = .{instr_sanitized} }},\n"
    zig_code += "        },\n"
    zig_code += "    },\n"
zig_code += "};\n"

# Output Zig code
print(zig_code)

# Extract all instruction names
instruction_names = sorted(instructions.keys())

# Generate Zig `enum` code
zig_enum = "const Instruction = enum {\n"
for name in instruction_names:
    zig_enum += f"    {sanitize(name)},\n"
zig_enum += "};\n"

# Output Zig code
print(zig_enum)
