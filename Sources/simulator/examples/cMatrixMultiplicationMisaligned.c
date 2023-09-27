int main()
{
  int matrix1[25] =  {1,  1,  1,  1,  1,
                      2,  2,  2,  2,  2,
                      3,  3,  3,  3,  3,
                      4,  4,  4,  4,  4,
                      5,  5,  5,  5,  5};
  int matrix2[25] =  {1,  2,  3,  4,  5,
                      1,  2,  3,  4,  5,
                      1,  2,  3,  4,  5,
                      1,  2,  3,  4,  5,
                      1,  2,  3,  4,  5};
  int resultMatrix[25];
  for (int i = 0; i < 5; i=i+1)
  {
    for(int j = 0; j < 5; j=j+1)
    {
      for(int k = 0; k < 5; k=k+1)
      {
        resultMatrix[i*5+j] = resultMatrix[i*5+j] + matrix1[i*5+k] * matrix2[k*5+j];
      }
    }
  }
}