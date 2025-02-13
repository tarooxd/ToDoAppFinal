package com.example.todoapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todoapp.R
import com.example.todoapp.viewModel.ToDoViewModel
import com.example.todoapp.model.ToDo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ToDoListPage(viewModel: ToDoViewModel, userId: String) {
    var showDialogAdd by remember {
        mutableStateOf(false)
    }
    var showDialog by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val errorMessage by viewModel.error.collectAsState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialogAdd = true },
                modifier = Modifier.alpha(0.5f),
            ) {
                Icon(
                    imageVector = Icons.Default.Add, contentDescription = "Add"
                )
            }
        }
    ) {

        val toDos by viewModel.todos.collectAsState()
        var selectedItem by remember {
            mutableStateOf(toDos?.firstOrNull())
        }
        var editedTitle by remember {
            mutableStateOf("")
        }
        var selectedStatus by remember { mutableStateOf(selectedItem?.status ?: false) }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
                .padding(it)
        ) {
            if (errorMessage == null) {
                toDos?.let {
                    LazyColumn(
                        modifier = Modifier.safeDrawingPadding(),
                        content = {
                            itemsIndexed(it) { index: Int, item: ToDo ->
                                ToDoItem(item = item, onClick = {
                                    selectedItem = item
                                    editedTitle = selectedItem?.title.orEmpty()
                                    selectedStatus = selectedItem?.status ?: false
                                    showDialog = true
                                })
                            }
                        }
                    )
                    LaunchedEffect(Unit) {
                        viewModel.getAllTodo(userId = userId)
                    }
                }
            } else {
                 Text(
                    text = "Por enquanto nada novo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        }
        if (showDialog) {
            Dialog(onDismissRequest = {
                showDialog = false
                editedTitle = ""
            }) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    OutlinedTextField(value = editedTitle, onValueChange = { newText ->
                        editedTitle = newText
                    })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Status")
                        Checkbox(
                            checked = selectedStatus,
                            onCheckedChange = { isChecked ->
                                selectedStatus = isChecked
                            }
                        )

                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            showDialog = false
                            scope.launch {
                                viewModel.deleteTodo(selectedItem!!.id, userId)
                                editedTitle = ""
                            }
                        }) {
                            Text(text = "Excluir")
                        }
                        Button(onClick = {
                            showDialog = false
                            scope.launch {
                                viewModel.editTodo(
                                    selectedItem!!.id,
                                    editedTitle,
                                    selectedStatus,
                                    userId
                                )
                                editedTitle = ""
                            }
                            selectedItem?.let {
                                it.status = selectedStatus
                            }

                        }) {
                            Text(text = "Editar")
                        }
                    }
                }
            }
        }
        if (showDialogAdd) {
            Dialog(onDismissRequest = { showDialogAdd = false }) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    OutlinedTextField(value = editedTitle, onValueChange = { newText ->
                        editedTitle = newText
                    })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            scope.launch {
                                viewModel.addTodo(editedTitle, userId)
                                editedTitle = ""
                            }
                            showDialogAdd = false
                        }) {
                            Text(text = "Adicionar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToDoItem(item: ToDo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(6.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(0.9f),
                    text = SimpleDateFormat("HH:mm:aa, dd/MM", Locale.ENGLISH).format(item.date),
                    fontSize = 16.sp,
                    color = Color.LightGray

                )
                Icon(
                    modifier = Modifier
                        .size(18.dp)
                        .weight(0.1f),
                    painter = painterResource(id = R.drawable.baseline_circle_24),
                    tint = if (item.status) Color.Green else Color(0xFFFF6666),
                    contentDescription = "Status"
                )
            }
            Text(
                text = item.title,
                fontSize = 20.sp,
                color = Color.White
            )

        }
    }
}