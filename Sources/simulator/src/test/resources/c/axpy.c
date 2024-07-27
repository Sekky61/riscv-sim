extern float a[];
extern float b[];
const float c = 10;

void main() { // Entry point. Needs arrays a and b of length 50 to be defined.
    for (int i = 0; i < 55; i++) {
        a[i] += b[i]*c;
    }
    return;
}