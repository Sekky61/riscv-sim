int main()
{
  int array [16] = {5,  6,  7,  1,  2,  1,  8,  4,
                    8,  4,  3,  9,  5,  5,  6,  7};
  int n = 16;
  int temp;
  for (int i = 0; i < n - 1; i = i+1)
    for (int j = 0; j < n - i - 1; j = j+1)
      if (array[j] > array[j + 1]){
	temp = array[j];
        array[j] = array[j + 1];
        array[j + 1] = temp;
      }
}