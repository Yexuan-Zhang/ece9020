# calc_v1.py
import tkinter as tk
import operator
import re

OPS = {'+': operator.add, '-': operator.sub,
       '*': operator.mul, '/': operator.truediv}

def left2right(expr: str) -> str:
    toks = re.findall(r'\d+\.\d+|\d+|[+\-*/]', expr)
    if not toks: raise ValueError
    res = float(toks[0])
    for op, num in zip(toks[1::2], toks[2::2]):
        res = OPS[op](res, float(num))
    return str(res)

class App:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title('Continuous Calculator')
        self.root.configure(bg='#f2f2f2')
        self.memory = 0.0  # 内存变量
        self.last_operator = None        # 当前保存的运算符
        self.operand = None              # 累积结果
        self.operator_buttons = {}       # 保存运算符按钮引用
        self.highlighted_op = None       # 当前被高亮的运算符按钮
        self.ready_for_new_input = False # 表示是否要清空屏幕开始新的数字输入
        self.just_calculated = False     # 刚刚计算完成的标志
        
        # 创建显示区域
        self.e = tk.Entry(self.root, font=('Arial', 28), bd=4,
                          relief='sunken', justify='right', width=15)
        self.e.grid(row=0, column=0, columnspan=4, pady=(10,5))
        
        # 内存状态显示
        self.mem_label = tk.Label(self.root, text='M: 0', font=('Arial', 12),
                                  bg='#f2f2f2', fg='#666')
        self.mem_label.grid(row=1, column=0, columnspan=4, sticky='w', padx=10)
        
        # 运算历史显示
        self.history_label = tk.Label(self.root, text='', font=('Arial', 10),
                                     bg='#f2f2f2', fg='#888', anchor='w')
        self.history_label.grid(row=2, column=0, columnspan=4, sticky='ew', padx=10)
        
        self._build()
    
    def _build(self):
        # 保持原有布局，在最后一排添加内存功能按钮
        buttons = ['7','8','9','/',
                   '4','5','6','*',
                   '1','2','3','-',
                   '0','.','=','+',
                   'M','MR','MC','C']  # 内存功能按钮放在最后一排
        
        for i, t in enumerate(buttons):
            r, c = divmod(i, 4)
            r += 3  # 从第4行开始（因为有更多显示区域）
            
            # 确定按钮颜色
            if t in OPS or t == '=':
                bg, fg = '#4d90fe', 'white'
            elif t in ['M', 'MR', 'MC']:
                bg, fg = '#ff6b6b', 'white'  # 内存按钮用红色
            elif t == 'C':
                bg, fg = '#ffa500', 'white'  # 清除按钮用橙色
            else:
                bg, fg = 'white', 'black'
            
            btn = tk.Button(self.root, text=t, width=5, height=2,
                           font=('Arial', 18), bg=bg, fg=fg,
                           activebackground='#d0e1ff',
                           command=lambda ch=t: self.on_click(ch))
            btn.grid(row=r, column=c, padx=4, pady=4)

            if t in OPS:
                self.operator_buttons[t] = btn  # 保存运算符按钮引用
            
            # 添加hover效果
            self._add_hover_effect(btn, bg, fg)
    
    def _add_hover_effect(self, button, original_bg, original_fg):
        def on_enter(event):
            # 如果这个按钮是当前高亮的运算符，不改变颜色
            if (self.highlighted_op and 
                button in self.operator_buttons.values() and
                self.operator_buttons.get(self.highlighted_op) == button):
                return
            if original_bg == '#4d90fe':  # 蓝色按钮（运算符）
                hover_bg = '#6ba3ff'
            elif original_bg == '#ff6b6b':  # 红色按钮（内存功能）
                hover_bg = '#ff8a8a'
            elif original_bg == '#ffa500':  # 橙色按钮（清除）
                hover_bg = '#ffb733'
            else:
                hover_bg = '#e6e6e6'
            button.config(bg=hover_bg)

        def on_leave(event):
            # 如果这个按钮是当前高亮的运算符，恢复高亮颜色
            if (self.highlighted_op and 
                button in self.operator_buttons.values() and
                self.operator_buttons.get(self.highlighted_op) == button):
                button.config(bg='#6bff90')  # 恢复高亮绿色
            else:
                # 否则恢复原始颜色
                button.config(bg=original_bg)

        button.bind('<Enter>', on_enter)
        button.bind('<Leave>', on_leave)

    def update_memory_display(self):
        """更新内存显示"""
        self.mem_label.config(text=f'M: {self.memory}')
    
    def update_history_display(self, text):
        """更新历史显示"""
        self.history_label.config(text=text)
    
    def on_click(self, ch):
        if ch in '0123456789.':
            if self.ready_for_new_input or self.just_calculated:
                self.e.delete(0, tk.END)  # 清空旧数字
                self.ready_for_new_input = False
                self.just_calculated = False
                if not self.last_operator:  # 如果没有待处理的运算符，清除高亮和历史
                    self._clear_op_highlight()
                    self.update_history_display('')
            self.e.insert(tk.END, ch)

        elif ch in OPS:
            # 连续运算逻辑
            try:
                current_value = float(self.e.get()) if self.e.get() else 0
                
                # 如果有待处理的运算，先执行它
                if self.operand is not None and self.last_operator:
                    result = OPS[self.last_operator](self.operand, current_value)
                    self.operand = result
                    self.e.delete(0, tk.END)
                    self.e.insert(0, f'{result:.10g}')
                    # 更新历史显示
                    self.update_history_display(f'{self.operand} {ch}')
                else:
                    # 第一次运算
                    self.operand = current_value
                    self.update_history_display(f'{self.operand} {ch}')
                
                # 设置新的运算符
                self.last_operator = ch
                self.ready_for_new_input = True
                
                # 更新按钮高亮
                self._clear_op_highlight()
                self.operator_buttons[ch].config(bg='#6bff90')  # 高亮绿色
                self.highlighted_op = ch
                
            except (ValueError, ZeroDivisionError):
                self.e.delete(0, tk.END)
                self.e.insert(0, 'Error')
                self._clear_state()

        elif ch == '=':
            try:
                if self.operand is not None and self.last_operator:
                    current_value = float(self.e.get()) if self.e.get() else 0
                    result = OPS[self.last_operator](self.operand, current_value)
                    self.e.delete(0, tk.END)
                    self.e.insert(0, f'{result:.10g}')
                    
                    # 更新历史显示为完整计算过程
                    self.update_history_display(f'{self.operand} {self.last_operator} {current_value} = {result:.10g}')
                    
                    # 保存结果作为下次计算的起点
                    self.operand = result
                    self.last_operator = None
                    self.just_calculated = True
                    self._clear_op_highlight()
            except (ValueError, ZeroDivisionError):
                self.e.delete(0, tk.END)
                self.e.insert(0, 'Error')
                self._clear_state()

        elif ch == 'C':
            self.e.delete(0, tk.END)
            self._clear_state()

        elif ch == 'MC':
            self.memory = 0.0
            self.update_memory_display()

        elif ch == 'MR':
            self.e.delete(0, tk.END)
            self.e.insert(0, f'{self.memory:.10g}')
            self.just_calculated = True

        elif ch == 'M':
            try:
                current_val = self.e.get()
                if current_val and current_val != 'Error':
                    self.memory = float(current_val)
                    self.update_memory_display()
            except ValueError:
                pass

    def _clear_state(self):
        """清除所有计算状态"""
        self.operand = None
        self.last_operator = None
        self.ready_for_new_input = False
        self.just_calculated = False
        self._clear_op_highlight()
        self.update_history_display('')

    def _clear_op_highlight(self):
        """取消上一个高亮的运算符按钮"""
        if self.highlighted_op:
            btn = self.operator_buttons.get(self.highlighted_op)
            if btn:
                btn.config(bg='#4d90fe')  # 恢复蓝色
            self.highlighted_op = None
    
    def run(self):
        self.root.mainloop()

if __name__ == '__main__':
    App().run()