#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
void sequence(int,int*);
int fibonacci(int);
void program();
char* intToString(int var){
    char* int_str = malloc(256);
    sprintf(int_str, "%d", var);
    return int_str;
}

char* doubleToString(double var){
    char* double_str = malloc(256);
    sprintf(double_str, "%f", var);
    return double_str;
}

char* boolToString(int var){
    if (var == 1){
        return "true";
    }
    if (var == 0){
        return "false";
    }
    return "";
}

char* concat(char* s1, char* i) {
    char* s = malloc(256);
    sprintf(s, "%s%s", s1, i);
    return s;
}
void sequence(int n,int* last){
int i = 1;
while(i < n + 1){
	printf("%d\n",fibonacci(i));
i = i + 1;
}
*last = fibonacci(n);
}
int fibonacci(int n){
int result;
result = n;
if(n > 1){
	result = fibonacci(n - 1) + fibonacci(n - 2);
}
return result;
}
void program(){
int n;
int last;
	printf("How many terms?\n");
	scanf("%d",&n);
sequence(n,&last); 
	printf("%s\n","Last term:");
	printf("%d\n",last);
}
int main(int argc, char** argv){ 
program();
return (EXIT_SUCCESS);
}
