import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

var currentPath = "c://"

@Composable
@Preview
fun App() {
    //State<List<T>>
    var fileList by mutableStateOf(emptyList<FileItem>())

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                //多选文件
                Button(onClick = {
                    fileList = openFile().toMutableList()

                }) {
                    Text("多选文件")
                }
                Button(modifier = Modifier.padding(start = 10.dp), onClick = {
                    fileList = emptyList()
                }) {
                    Text("清空列表")
                    fileList = emptyList<FileItem>().toMutableList()
                }
                Button(modifier = Modifier.padding(start = 10.dp), onClick = {
                    fileList.filter { it.status == "×" }.map {
                        val absoluteFile = it.file.absoluteFile
                        val parentFile = absoluteFile.parentFile
                        val newFile = File(parentFile, it.preview)
                        it.status = if (absoluteFile.renameTo(newFile)) "√" else "×"
                    }
                }) {
                    Text("开始转换")
                }
            }
            //列表显示文件夹
            LazyColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                item {
                    Row(Modifier.fillMaxWidth()) {
                        TableCell("文件名", .4f)
                        TableCell("类型", .1f)
                        TableCell("更改预览", .4f)
                        TableCell("状态", .1f)
                    }
                }
                items(fileList.size) {
                    Row(Modifier.fillMaxWidth()) {
                        TableCell(fileList[it].name, .4f)
                        TableCell(fileList[it].type, .1f)
                        TableCell(fileList[it].preview, .4f)
                        TableCell(fileList[it].status, .1f)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp),
        maxLines = 1
    )
}

fun openFile(): List<FileItem> {
    val fc = JFileChooser(currentPath)
    fc.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
    fc.isMultiSelectionEnabled = true
    val returnVal = fc.showOpenDialog(null)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        val file = fc.selectedFiles
        val fileList = ArrayList<FileItem>()
        if (!file.indices.isEmpty()) {
            currentPath = file[0].parent
        }
        for (i in file.indices) {
            val fileItem = file2fileItem(file[i])
            fileList.add(fileItem)
        }
        return fileList
    }
    return emptyList()
}

fun main() = application {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

data class FileItem(
    val name: String,
    val type: String,
    val preview: String,
    val file: File,
    var status: String = "×"
)

/**
 * 从gbk编码读取，重写为cp932编码
 * @param str
 */
fun gbk2Cp932(str: String): String {
    return String(str.toByteArray(charset("gbk")), charset("cp932"))
}

/**
 * 将File转换为FileItem
 */
fun file2fileItem(
    file: File
): FileItem {
    return FileItem(
        name = file.name,
        type = if (file.isDirectory) "文件夹" else "文件",
        preview = gbk2Cp932(file.name),
        file = file
    )
}
