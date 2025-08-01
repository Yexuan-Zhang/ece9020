# calc_v1_rpn.py
import tkinter as tk
import operator
import re

OPS = {'+': operator.add, '-': operator.sub,
       '*': operator.mul, '/': operator.truediv}

class App:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title('RPN Calculator')
        self.root.configure(bg='#f2f2f2')
        self.memory = 0.0  
        self.stack = []    
        self.operator_buttons = {}      
        self.highlighted_op = None       
        self.ready_for_new_input = False 
        
        
        self.e = tk.Entry(self.root, font=('Arial', 28), bd=4,
                          relief='sunken', justify='right', width=15)
        self.e.grid(row=0, column=0, columnspan=4, pady=(10,5))
        
        
        self.stack_label = tk.Label(self.root, text='Stack: []', font=('Arial', 10),
                                   bg='#f2f2f2', fg='#333', anchor='w')
        self.stack_label.grid(row=1, column=0, columnspan=4, sticky='ew', padx=10)
        
        
        self.mem_label = tk.Label(self.root, text='M: 0', font=('Arial', 12),
                                  bg='#f2f2f2', fg='#666', anchor='w')
        self.mem_label.grid(row=2, column=0, columnspan=4, sticky='ew', padx=10)
        
        self._build()
        self.update_stack_display()
    
    def _build(self):
        
        buttons = ['7','8','9','/',
                   '4','5','6','*',
                   '1','2','3','-',
                   '0','.','EN','+',
                   'M','MR','MC','C']  
        
        for i, t in enumerate(buttons):
            r, c = divmod(i, 4)
            r += 3  
            
            
            if t in OPS or t == 'EN':
                bg, fg = '#4d90fe', 'white'
            elif t in ['M', 'MR', 'MC']:
                bg, fg = '#ff6b6b', 'white'  
            elif t == 'C':
                bg, fg = '#ffa500', 'white'  
            else:
                bg, fg = 'white', 'black'
            
            btn = tk.Button(self.root, text=t, width=5, height=2,
                           font=('Arial', 18), bg=bg, fg=fg,
                           activebackground='#d0e1ff',
                           command=lambda ch=t: self.on_click(ch))
            btn.grid(row=r, column=c, padx=4, pady=4)

            if t in OPS:
                self.operator_buttons[t] = btn  
            
            
            self._add_hover_effect(btn, bg, fg)
    
    def _add_hover_effect(self, button, original_bg, original_fg):
        def on_enter(event):
            
            if (self.highlighted_op and 
                button in self.operator_buttons.values() and
                self.operator_buttons.get(self.highlighted_op) == button):
                return
            
            if original_bg == '#4d90fe':  
                hover_bg = '#6ba3ff'
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
    
    def update_stack_display(self):
       
        if self.stack:
           
            display_stack = self.stack[-3:] if len(self.stack) > 3 else self.stack
            stack_str = ' '.join([f'{x:.6g}' for x in display_stack])
            if len(self.stack) > 3:
                stack_str = '... ' + stack_str
            self.stack_label.config(text=f'Stack: [{stack_str}]')
        else:
            self.stack_label.config(text='Stack: []')
    
    def on_click(self, ch):
        if ch in '0123456789.':
            if self.ready_for_new_input:
                self.e.delete(0, tk.END)  
                self.ready_for_new_input = False  
                self._clear_op_highlight()  
            self.e.insert(tk.END, ch)

        elif ch in OPS:
            if len(self.stack) == 0:
                return
            
            current = self.e.get().strip()
            if current:
                try:
                    self.stack.append(float(current))
                    self.e.delete(0, tk.END)
                    self.update_stack_display()
                except ValueError:
                    return
            
            if len(self.stack) >= 2:
                try:
                    b = self.stack.pop()
                    a = self.stack.pop()
                    result = OPS[ch](a, b)
                    self.stack.append(result)
                    self.e.delete(0, tk.END)
                    self.e.insert(0, f'{result:.6g}')
                    self.update_stack_display()
                    self.ready_for_new_input = True
                    
                  
                    self._clear_op_highlight()
                    self.operator_buttons[ch].config(bg='#6bff90')  
                    self.highlighted_op = ch
                    
                except (ZeroDivisionError, ValueError):
                    self.e.delete(0, tk.END)
                    self.e.insert(0, 'Error')
                    self.stack.clear()
                    self.update_stack_display()
                    self._clear_op_highlight()

        elif ch == 'EN':
           
            current = self.e.get().strip()
            if current and current != 'Error':
                try:
                    self.stack.append(float(current))
                    self.e.delete(0, tk.END)
                    self.update_stack_display()
                    self.ready_for_new_input = True
                except ValueError:
                    pass
            self._clear_op_highlight()

        elif ch == 'C':
            
            self.e.delete(0, tk.END)
            self.stack.clear()
            self.update_stack_display()
            self.ready_for_new_input = False
            self._clear_op_highlight()

        elif ch == 'MC':
           
            self.memory = 0.0
            self.update_memory_display()
            self._clear_op_highlight()

        elif ch == 'MR':
            
            self.e.delete(0, tk.END)
            self.e.insert(0, f'{self.memory:.6g}')
            self.ready_for_new_input = True
            self._clear_op_highlight()

        elif ch == 'M':
            
            try:
                current_val = self.e.get().strip()
                if current_val and current_val != 'Error':
                    self.memory = float(current_val)
                elif self.stack:
                  
                    self.memory = self.stack[-1]
                self.update_memory_display()
            except (ValueError, IndexError):
                pass
            self._clear_op_highlight()

    def _clear_op_highlight(self):
        
        if self.highlighted_op:
            btn = self.operator_buttons.get(self.highlighted_op)
            if btn:
                btn.config(bg='#4d90fe') 
            self.highlighted_op = None
    
    def run(self):
        self.root.mainloop()

if __name__ == '__main__':
    App().run()
