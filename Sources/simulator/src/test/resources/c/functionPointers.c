typedef struct {
    void (*draw)(int *); // Function pointer
} Shape;

Shape arr[4];

void __attribute__ ((noinline)) drawCircle(int *x) {
    *x *= 2;
}

void __attribute__ ((noinline)) drawRectangle(int *x) {
    *x += 1;
}

void __attribute__ ((noinline)) init() {
  Shape g = {drawCircle};
  Shape g2 = {drawRectangle};
  arr[0] = g;
  arr[1] = g2;
  arr[2] = g;
  arr[3] = g2;
}

int main() {
    init();
    int volatile x = 0;

    for(int i = 0; i < 4; i++) {
      arr[i].draw(&x);
    }

    return x;
}
