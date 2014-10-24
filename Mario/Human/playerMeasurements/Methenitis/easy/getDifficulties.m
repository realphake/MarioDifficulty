clear all

file = fopen('difficulties.txt');
A=fscanf(file,'%f %f',[5 inf]);
A=A';
figure;
plot(A);
legend('STRAIGHT','HILLS','TUBES','JUMP','CANNONS','Location','northwest');
xlabel('Time (No. of iterations)');
ylabel('Difficulty level');

saveTightFigure('metheUnsupervised.png');