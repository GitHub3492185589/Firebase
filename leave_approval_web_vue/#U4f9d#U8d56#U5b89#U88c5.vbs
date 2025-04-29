' VBScript to execute a command in the current directory via cmd.exe  VBScript 在当前目录下通过 cmd.exe 执行命令
 
Option Explicit

Dim objShell, strCommand, strCurrentDir, strTitle

' Create the shell object			创建 Shell 对象
Set objShell = CreateObject("WScript.Shell")

' Get the current directory			获取当前目录
strCurrentDir = objShell.CurrentDirectory

' Set a title for the command prompt window		设置命令提示窗口的标题
strTitle = "Command Executor"

' Prompt the user for the command to execute	提示用户输入要执行的命令
strCommand = InputBox("Enter the command to execute:", strTitle, "npm i") ' Default command is "dir"

' Check if the user entered a command			检查用户是否输入了命令
If strCommand = "" Then
    WScript.Echo "No command entered. Exiting."
    WScript.Quit
End If

' Construct the command line to run cmd.exe		构造要执行的命令行
Dim strCommandLine
strCommandLine = "%comspec% /c cd /d """ & strCurrentDir & """ && " & strCommand

' Execute the command in cmd.exe				 在 cmd.exe 中执行命令
objShell.Run strCommandLine, , False  ' The 'False' argument makes the script wait until the command is finished

' Clean up the object							 可选：显示执行完成的消息框（如需静默执行可删除此行）
Set objShell = Nothing

' Optional: Message box to indicate completion (can be removed for silent execution)
WScript.Echo "Command executed successfully."

WScript.Quit