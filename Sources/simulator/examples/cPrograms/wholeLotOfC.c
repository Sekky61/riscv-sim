int globalVar;

int asdf2(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j, int k, int l){
 return a * (b + d - (c + a) / 2) * e * (f + g - (h + i) / 2) + j * (k + l - (a + d) / 2);
}

int asdf(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j, int k, int l){
 globalVar = a * (b + d - (c + a) / 2) * e * (f + g - (h + i) / 2) + j * (k + l - (a + d) / 2) - asdf2(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
 return a+b;
}

int main (){
 int i, j,k, o, p, q = 0;
 float l, m, n = 1.0;
 i = (int)l;
 for(i=0;i<=3;i=i+1)
  if(i >= 2){
  j = j + i;
 }
 else
  j = i;
 o = asdf(i, j, asdf(o,p,k,(4+3)*5/(1+4), 5, 6, 7, 8, 9, 10, 11, 12), 4, 5, 6, 7, 8, 9, 10, 11, 12);
 k = k + i + j;
 return i;
}