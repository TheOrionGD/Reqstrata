import os
import re

files_to_process = [
    "app/src/main/java/com/example/ui/screens/design/DesignersScreens.kt",
    "app/src/main/java/com/example/ui/screens/tasks/TaskManagementScreen.kt",
    "app/src/main/java/com/example/ui/screens/requirements/RequirementAiAnalysisScreen.kt",
    "app/src/main/java/com/example/ui/screens/requirements/RequirementComparisonScreen.kt",
    "app/src/main/java/com/example/ui/screens/requirements/GenerateRequirementsScreen.kt",
    "app/src/main/java/com/example/ui/screens/requirements/NewRequirementFormScreen.kt",
    "app/src/main/java/com/example/ui/screens/requirements/BusinessAnalystDashboardScreen.kt",
    "app/src/main/java/com/example/ui/screens/requirements/UseCaseDetailScreen.kt",
    "app/src/main/java/com/example/ui/screens/requirements/RequirementsScreens.kt",
    "app/src/main/java/com/example/ui/screens/project/ProjectOverviewScreen.kt",
    "app/src/main/java/com/example/ui/screens/project/ProjectScreens.kt",
    "app/src/main/java/com/example/ui/screens/project/ProjectSettingsScreen.kt",
    "app/src/main/java/com/example/ui/screens/project/ProjectDashboardScreen.kt",
    "app/src/main/java/com/example/ui/screens/testing/CoverageDashboardScreen.kt",
    "app/src/main/java/com/example/ui/screens/testing/GenerateTestSuiteScreen.kt",
    "app/src/main/java/com/example/ui/screens/testing/TestingWorkspaceScreen.kt",
    "app/src/main/java/com/example/ui/screens/team/RoleManagementScreen.kt",
    "app/src/main/java/com/example/ui/screens/team/TeamAndActivityScreens.kt",
    "app/src/main/java/com/example/ui/screens/team/MemberDetailScreen.kt",
    "app/src/main/java/com/example/ui/screens/team/InviteMemberScreen.kt",
    "app/src/main/java/com/example/ui/screens/documents/DocumentScreens.kt",
    "app/src/main/java/com/example/ui/screens/architecture/ArchitectureDecisionsScreen.kt",
    "app/src/main/java/com/example/ui/screens/architecture/SuggestArchitectureScreen.kt",
    "app/src/main/java/com/example/ui/screens/architecture/ArchitectureScreens.kt",
    "app/src/main/java/com/example/ui/screens/architecture/ArchitectureComponentDetailScreen.kt",
    "app/src/main/java/com/example/ui/screens/ai/AiAssistantScreens.kt",
    "app/src/main/java/com/example/ui/screens/traceability/TraceabilityScreens.kt",
    "app/src/main/java/com/example/ui/components/UmlDiagramCanvas.kt",
    "app/src/main/java/com/example/data/remote/mongo/stitch/MongoDocument.kt",
    "app/src/main/java/com/example/data/local/entity/Entities.kt",
    "app/src/main/java/com/example/data/repository/Repositories.kt",
]

replacements = [
    # Primary brand blues -> Violet palette
    ("0xFF0284C7", "0xFF6D28D9"), # Primary Violet
    ("0xFF38BDF8", "0xFF8B5CF6"), # Primary Light
    ("0xFF0369A1", "0xFF5B21B6"), # Primary Dark
    ("0xFF60A5FA", "0xFF8B5CF6"), # Primary Light
    ("0xFF0EA5E9", "0xFF8B5CF6"), # Primary Light
    ("0xFF1E3A8A", "0xFF2E1065"), # Dark Primary Container
    ("0xFF4F46E5", "0xFF6D28D9"), # Primary Violet
    ("0xFF6366F1", "0xFF7C3AED"), # AI Accent
    ("0xFF818CF8", "0xFF8B5CF6"), # Primary Light

    # Dark background & surfaces (navy/slate -> dark graphite palette)
    ("0xFF0A0F1D", "0xFF09090B"), # Dark Background
    ("0xFF0F172A", "0xFF18181B"), # Dark Surface
    ("0xFF1E293B", "0xFF27272A"), # Dark Elevated Surface
    ("0xFF131D33", "0xFF27272A"), # Dark Elevated Surface
    ("0xFF334155", "0xFF3F3F46"), # Dark Border
    ("0xFF94A3B8", "0xFFA1A1AA"), # Dark Text Secondary
    ("0xFF64748B", "0xFF71717A"), # Dark Text Muted
    ("0xFFCBD5E1", "0xFFFAFAFA"), # Dark Text Primary
]

for file_path in files_to_process:
    full_path = "/" + file_path.lstrip("/")
    if not os.path.exists(full_path):
        print(f"File not found: {full_path}")
        continue
    with open(full_path, "r", encoding="utf-8") as f:
        content = f.read()

    new_content = content
    for old_val, new_val in replacements:
        new_content = new_content.replace(old_val, new_val)

    if new_content != content:
        with open(full_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"Updated: {file_path}")
    else:
        print(f"No changes in: {file_path}")

print("Replacement complete.")
