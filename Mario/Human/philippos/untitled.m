% file = fopen('difficulties.txt');
% A=fscanf(file,'%f %f',[5 inf]);
% A=A';
% A=A(:,:)


% figure()
% plot(A);
% legend('STRAIGHT','HILLS','TUBES','JUMPS','CANNONS','Location','northwest');
% xlabel('Time (Number of iterations)');
% ylabel('Difficulty levels');
% saveTightFigure('difficulties.png')

file = fopen('section4Emotions.txt');
b=fscanf(file,'%f %f',[7 inf]);
b=b';

figure()
plot(b);
legend('neutral','happy','surprised','angry','disgusted','afraid','sad');
xlabel('Time (Number of frames)');
ylabel('Emotion probability estimates');
saveTightFigure('section4Emotions.png')
