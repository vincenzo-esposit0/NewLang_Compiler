#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
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
void program(){
int n;
int flag = 0;
	printf("%s","a");
	printf("%s","b");
	printf("%s","c");
	printf("%s","d");
	printf("\n");
	printf("%s",boolToString(flag));
	printf("%s"," ");
	printf("%s",boolToString(1));
	printf("\n");
	printf("number:\n");
	scanf("%d",&n);
	printf("%s","n=");
	printf("%d",n);
	printf("%s","");
	printf("\n");
	printf("%s","end");
}
int main(int argc, char** argv){ 
program();
return (EXIT_SUCCESS);
}
