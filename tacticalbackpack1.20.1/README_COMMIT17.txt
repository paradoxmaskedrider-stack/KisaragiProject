TacticalBackpack commit17 hotfix

Fix:
- Removes the obsolete private AE2BackpackActionPacket() {} constructor.
- This resolves: variable action might not have been initialized.

Apply:
1. Extract this ZIP anywhere.
2. Open PowerShell in E:\github\KisaragiProject\tacticalbackpack1.20.1
3. Run the extracted apply_commit17_hotfix.ps1, or manually copy the included Java file over the matching project path.
4. Verify:
   Select-String .\src\main\java\com\github\kisaragimikoto\tacticalbackpack\network\AE2BackpackActionPacket.java -Pattern 'private AE2BackpackActionPacket'
   It should return nothing.
5. Run:
   .\gradlew.bat clean
   .\gradlew.bat runClient
