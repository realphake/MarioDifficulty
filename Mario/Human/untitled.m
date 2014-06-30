file = fopen('difficulties.txt');
A=fscanf(file,'%f %f',[5 inf]);
A=A';
A=A(:,:)


% file = fopen('allEmotions.txt');
% b=fscanf(file,'%f %f',[7 inf]);
% b=b';

figure()
plot(A);
legend('STRAIGHT','HILLS','TUBES','JUMPS','CANNONS','Location','northwest');

% figure()
% plot(b);
% legend('neutral','happy','surprised','angry','disgusted','afraid','sad');
