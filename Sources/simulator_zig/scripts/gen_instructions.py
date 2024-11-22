import json
import sys
from dataclasses import dataclass, field
from typing import List, Dict, Tuple

# Mapping fields to instruction layouts
field_to_type = {
    frozenset(["rd", "imm20"]): "U",
    frozenset(["rd", "jimm20"]): "J",
    frozenset(['bimm12hi', 'rs1', 'rs2', 'bimm12lo']): "B",
    frozenset(['imm12hi', 'rs1', 'rs2', 'imm12lo']): "S",
    frozenset(["imm", "rs1", "rs2"]): "S",
    frozenset(["rd", "rs1", "rs2"]): "R",
    frozenset(["rd", "rs1", "imm12"]): "I",
    frozenset(["rd", "rs1", "imm"]): "I",
}

def instructionFilter(instruction: str) -> bool:
    """Filter out instructions that we don't want to include."""
    banned = [
        "ebreak",
        "ecall",
        "wfi",
        "fence",
        "sfence",
        "csrr",
        "csrw",
        "csrs",
        "csrc",
    ]
    if instruction in banned:
        return False
    return True

@dataclass
class InstructionEntry:
    """Represents an instruction match and name."""
    match: str
    instruction: str

@dataclass
class MaskGroup:
    """Holds a group of instructions for a specific mask and layout."""
    mask: str
    layout: str
    entries: List[InstructionEntry] = field(default_factory=list)

def parse_json(data: str) -> Dict[Tuple[str, str], MaskGroup]:
    """Parses JSON and groups instructions by mask and layout."""
    instructions = json.loads(data)
    groups = {}
    for name, details in instructions.items():
        # Filter
        if not instructionFilter(name):
            continue
        mask = details["mask"]
        layout = field_to_type.get(frozenset(details["variable_fields"]), "Unknown")
        if layout == "Unknown":
            print(f"Unknown layout for instruction {name}: {details['variable_fields']}")
        key = (mask, layout)
        if key not in groups:
            groups[key] = MaskGroup(mask=mask, layout=layout)
        groups[key].entries.append(InstructionEntry(match=details["match"], instruction=name))

    return groups

def sanitize(name: str) -> str:
    """Sanitizes instruction names for Zig compatibility."""
    if name in {"break", "or", "and"}:
        return f"@\"{name}\""
    return name

def generate_zig_encodings(groups: Dict[Tuple[str, str], MaskGroup]) -> str:
    """Generates Zig encoding array."""
    zig_code = "const encodings: []const DecodingTable([]const InstructionEncoding) = &.{\n"
    for group in groups.values():
        zig_code += f"    .{{\n"
        zig_code += f"        .mask = {group.mask},\n"
        zig_code += f"        .layout = .{group.layout},\n"
        zig_code += "        .table = &.{\n"
        for entry in sorted(group.entries, key=lambda x: int(x.match, 16)):
            instr_sanitized = sanitize(entry.instruction)
            zig_code += f"            .{{ .match = {entry.match}, .instruction = .{instr_sanitized} }},\n"
        zig_code += "        },\n"
        zig_code += "    },\n"
    zig_code += "};\n"
    return zig_code

def generate_zig_enum(instructions: set[str]) -> str:
    """Generates Zig enum for instructions."""
    zig_enum = "const Opcode = enum {\n"
    for name in sorted(instructions):
        zig_enum += f"    {sanitize(name)},\n"
    zig_enum += "};\n"
    return zig_enum

# Read JSON from stdin
json_data = sys.stdin.read()

# Parse JSON and group instructions
mask_groups = parse_json(json_data)

# Generate Zig code
zig_encodings = generate_zig_encodings(mask_groups)
all_instructions = {entry.instruction for group in mask_groups.values() for entry in group.entries}
zig_enum = generate_zig_enum(all_instructions)

# Print Zig output
print(zig_encodings)
print(zig_enum)
