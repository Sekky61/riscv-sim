int resultMatrix[16];

int matrix1[16] =  {1,  1,  1,  1,
                    2,  2,  2,  2,
                    3,  3,  3,  3,
                    4,  4,  4,  4};
int matrix2[16] =  {1,  2,  3,  4,
                    1,  2,  3,  4,
                    1,  2,  3,  4,
                    1,  2,  3,  4};

int main() // Entry point
{
    for (int i = 0; i < 4; i++)
    {
        for(int j = 0; j < 4; j++)
        {
            for(int k = 0; k < 4; k++)
            {
                resultMatrix[i*4+j] = resultMatrix[i*4+j] + matrix1[i*4+k] * matrix2[k*4+j];
            }
        }
    }
}