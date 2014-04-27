file = fopen('difficulties.txt');
A=fscanf(file,'%f %f',[5 inf]);
A=A';
plot(A);
legend('STRAIGHT','HILLS','TUBES','JUMPS','CANNONS','Location','northwest');