import tkinter as tk
import operator
import re

OPS = {'+': operator.add, '-': operator.sub,
       '*': operator.mul, '/': operator.truediv}

def left2right(expr: str) -> str:
    """从左到右计算表达式，不考虑运算符优先级"""
    toks = re.findall(r'\d+\.\d+|\d+|[+\-*/]', expr)
    if not toks: 
        raise ValueError
    res = float(toks[0])
    for op, num in zip(toks[1::2], toks[2::2]):
        res = OPS[op](res, float(num))
    return res

def evaluate_with_parentheses(expr: str) -> float:
    """处理括号表达式，括号内优先计算，但括号内仍然从左到右"""
    expr = expr.replace(' ', '')
    
    # 递归处理最内层括号
    while '(' in expr:
        start = -1
        for i, char in enumerate(expr):
            if char == '(':
                start = i
            elif char == ')':
                if start == -1:
                    raise ValueError("括号不匹配")
                inner_expr = expr[start+1:i]
                if not inner_expr:
                    raise ValueError("空括号")
                result = left2right(inner_expr)
                expr = expr[:start] + str(result) + expr[i+1:]
                break
        else:
            if start != -1:
                raise ValueError("括号不匹配")
    
    return left2right(expr)

class Calculator:
    def __init__(self, root):
        self.root = root
        self.root.title("Continuous Calculator with Parentheses")
        self.root.configure(bg='#f2f2f2')
        
        # 创建显示区域
        self.display = tk.Entry(root, font=('Arial', 28), bd=4,
                               relief='sunken', justify='right', width=15)
        self.display.grid(row=0, column=0, columnspan=4, pady=(10,5))
        
        # 内存状态显示
        self.mem_label = tk.Label(root, text='M: 0', font=('Arial', 12),
                                  bg='#f2f2f2', fg='#666')
        self.mem_label.grid(row=1, column=0, columnspan=4, sticky='w', padx=10)
        
        # 运算历史显示
        self.history_label = tk.Label(root, text='', font=('Arial', 10),
                                     bg='#f2f2f2', fg='#888', anchor='w')
        self.history_label.grid(row=2, column=0, columnspan=4, sticky='ew', padx=10)

        self.memory = 0.0
        self.last_operator = None
        self.operand = None
        self.operator_buttons = {}
        self.highlighted_op = None
        self.ready_for_new_input = False
        self.just_calculated = False
        
        # 括号相关状态
        self.bracket_stack = []  # 存储括号内的计算状态
        self.current_expression = ""  # 当前完整表达式（用于历史显示）
        self.in_bracket = False
        self.bracket_result_ready = False  # 标记括号结果是否已计算完成
        self.pending_bracket_result = None  # 存储已计算的括号结果

        # 按钮布局
        buttons = [
            '(', ')', 'C', 'AC',
            '7', '8', '9', '/',
            '4', '5', '6', '*',
            '1', '2', '3', '-',
            '0', '.', '=', '+',
            'M', 'MR', 'MC', 'Del'
        ]

        # 创建按钮
        for i, text in enumerate(buttons):
            row = (i // 4) + 3
            col = i % 4
            
            # 确定按钮颜色
            if text in OPS or text == '=':
                bg, fg = '#4d90fe', 'white'
            elif text in ['(', ')']:
                bg, fg = '#9c27b0', 'white'
            elif text in ['M', 'MR', 'MC']:
                bg, fg = '#ff6b6b', 'white'
            elif text in ['C', 'AC', 'Del']:
                bg, fg = '#ffa500', 'white'
            else:
                bg, fg = 'white', 'black'
            
            button = tk.Button(root, text=text, width=5, height=2, 
                             font=('Arial', 18), bg=bg, fg=fg,
                             activebackground='#d0e1ff',
                             command=lambda t=text: self.on_button_click(t))
            button.grid(row=row, column=col, padx=4, pady=4)
            
            if text in OPS:
                self.operator_buttons[text] = button
            
            self._add_hover_effect(button, bg, fg)

    def _add_hover_effect(self, button, original_bg, original_fg):
        def on_enter(event):
            if (self.highlighted_op and 
                button in self.operator_buttons.values() and
                self.operator_buttons.get(self.highlighted_op) == button):
                return
            
            if original_bg == '#4d90fe':
                hover_bg = '#6ba3ff'
            elif original_bg == '#9c27b0':
                hover_bg = '#ba68c8'
            elif original_bg == '#ff6b6b':
                hover_bg = '#ff8a8a'
            elif original_bg == '#ffa500':
                hover_bg = '#ffb733'
            else:
                hover_bg = '#e6e6e6'
            button.config(bg=hover_bg)

        def on_leave(event):
            if (self.highlighted_op and 
                button in self.operator_buttons.values() and
                self.operator_buttons.get(self.highlighted_op) == button):
                button.config(bg='#6bff90')
            else:
                button.config(bg=original_bg)

        button.bind('<Enter>', on_enter)
        button.bind('<Leave>', on_leave)

    def update_memory_display(self):
        self.mem_label.config(text=f'M: {self.memory}')
    
    def update_history_display(self, text):
        self.history_label.config(text=text)

    def on_button_click(self, char):
        if char in '0123456789.':
            # 数字输入
            if self.ready_for_new_input or self.just_calculated or self.bracket_result_ready:
                self.display.delete(0, tk.END)
                self.ready_for_new_input = False
                self.just_calculated = False
                self.bracket_result_ready = False
                if not self.in_bracket and not self.last_operator:
                    self._clear_op_highlight()
                    self.update_history_display('')
            self.display.insert(tk.END, char)
            
            # 更新表达式记录
            if self.current_expression and self.current_expression[-1] in '+-*/(':
                self.current_expression += char
            elif not self.current_expression:
                self.current_expression = char
            else:
                # 替换当前数字
                i = len(self.current_expression) - 1
                while i >= 0 and self.current_expression[i] not in '+-*/()':
                    i -= 1
                self.current_expression = self.current_expression[:i+1] + self.display.get()

        elif char == '(':
            # 左括号 - 保存当前状态并开始新的计算
            if self.bracket_result_ready:
                # 如果已有括号结果，先处理它
                current_value = self.pending_bracket_result
            else:
                current_value = float(self.display.get()) if self.display.get() else 0
            
            # 保存当前状态到栈
            state = {
                'operand': self.operand,
                'operator': self.last_operator,
                'value': current_value,
                'outer_expression': self.current_expression
            }
            self.bracket_stack.append(state)
            
            # 重置当前计算状态
            self.operand = None
            self.last_operator = None
            self.ready_for_new_input = True
            self.in_bracket = True
            self.bracket_result_ready = False
            self.pending_bracket_result = None
            
            # 更新表达式和历史
            if not self.current_expression:
                self.current_expression = str(current_value) + '('
            else:
                self.current_expression += '('
            self.update_history_display(self.current_expression)

        elif char == ')':
            # 右括号 - 计算括号内结果并显示，但不立即与外部计算
            if not self.bracket_stack:
                return  # 没有对应的左括号
            
            # 完成括号内的计算
            current_value = float(self.display.get()) if self.display.get() else 0
            if self.operand is not None and self.last_operator:
                bracket_result = OPS[self.last_operator](self.operand, current_value)
            else:
                bracket_result = current_value
            
            # 恢复之前的状态
            state = self.bracket_stack.pop()
            outer_operand = state['operand']
            outer_operator = state['operator']
            outer_expression = state['outer_expression']
            
            # 显示括号计算结果
            self.display.delete(0, tk.END)
            self.display.insert(0, f'{bracket_result:.10g}')
            
            # 更新表达式
            self.current_expression += ')'
            self.update_history_display(self.current_expression)
            
            # 设置状态：括号结果已准备好，等待后续操作
            self.operand = outer_operand
            self.last_operator = outer_operator
            self.pending_bracket_result = bracket_result
            self.bracket_result_ready = True
            self.ready_for_new_input = False
            
            # 如果没有更多括号，退出括号模式
            if not self.bracket_stack:
                self.in_bracket = False

        elif char in OPS:
            # 运算符 - 如果有准备好的括号结果，现在进行计算
            try:
                if self.bracket_result_ready:
                    # 使用括号结果进行计算
                    current_value = self.pending_bracket_result
                    self.bracket_result_ready = False
                    self.pending_bracket_result = None
                else:
                    current_value = float(self.display.get()) if self.display.get() else 0
                
                if self.operand is not None and self.last_operator:
                    result = OPS[self.last_operator](self.operand, current_value)
                    self.operand = result
                    self.display.delete(0, tk.END)
                    self.display.insert(0, f'{result:.10g}')
                else:
                    self.operand = current_value
                
                self.last_operator = char
                self.ready_for_new_input = True
                
                # 更新表达式
                if not self.current_expression:
                    self.current_expression = str(self.operand) + char
                else:
                    self.current_expression += char
                self.update_history_display(self.current_expression)
                
                # 更新按钮高亮
                self._clear_op_highlight()
                self.operator_buttons[char].config(bg='#6bff90')
                self.highlighted_op = char
                
            except (ValueError, ZeroDivisionError):
                self.display.delete(0, tk.END)
                self.display.insert(0, 'Error')
                self._clear_state()

        elif char == '=':
            try:
                if self.bracket_result_ready:
                    # 使用括号结果进行最终计算
                    current_value = self.pending_bracket_result
                    self.bracket_result_ready = False
                    self.pending_bracket_result = None
                else:
                    current_value = float(self.display.get()) if self.display.get() else 0
                
                # 如果在括号中，先完成括号内计算
                while self.bracket_stack:
                    if self.operand is not None and self.last_operator:
                        bracket_result = OPS[self.last_operator](self.operand, current_value)
                    else:
                        bracket_result = current_value
                    
                    state = self.bracket_stack.pop()
                    self.operand = state['operand']
                    self.last_operator = state['operator']
                    current_value = bracket_result
                
                # 完成最终计算
                if self.operand is not None and self.last_operator:
                    result = OPS[self.last_operator](self.operand, current_value)
                    self.display.delete(0, tk.END)
                    self.display.insert(0, f'{result:.10g}')
                    
                    self.update_history_display(f'{self.current_expression} = {result:.10g}')
                    
                    self.operand = result
                    self.last_operator = None
                    self.just_calculated = True
                
                self.in_bracket = False
                self._clear_op_highlight()
                        
            except (ValueError, ZeroDivisionError):
                self.display.delete(0, tk.END)
                self.display.insert(0, 'Error')
                self._clear_state()

        elif char == 'C':
            self.display.delete(0, tk.END)

        elif char == 'AC':
            self.display.delete(0, tk.END)
            self._clear_state()

        elif char == 'Del':
            current = self.display.get()
            if current:
                self.display.delete(len(current)-1, tk.END)

        elif char == 'M':
            try:
                current_val = self.display.get().strip()
                if current_val and current_val not in ['Error', 'Invalid']:
                    self.memory = float(current_val)
                    self.update_memory_display()
            except ValueError:
                pass

        elif char == 'MR':
            self.display.delete(0, tk.END)
            self.display.insert(0, f'{self.memory:.10g}')
            self.just_calculated = True

        elif char == 'MC':
            self.memory = 0.0
            self.update_memory_display()

    def _clear_state(self):
        self.operand = None
        self.last_operator = None
        self.ready_for_new_input = False
        self.just_calculated = False
        self.bracket_stack = []
        self.current_expression = ""
        self.in_bracket = False
        self.bracket_result_ready = False
        self.pending_bracket_result = None
        self._clear_op_highlight()
        self.update_history_display('')

    def _clear_op_highlight(self):
        if self.highlighted_op:
            btn = self.operator_buttons.get(self.highlighted_op)
            if btn:
                btn.config(bg='#4d90fe')
            self.highlighted_op = None


if __name__ == '__main__':
    root = tk.Tk()
    calc = Calculator(root)
    root.mainloop()