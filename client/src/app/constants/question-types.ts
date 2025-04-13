interface QuestionType {
    translocoLabel: string;
    translocoLongLabel: string;
    htmlLabel: string;
}

export const QCM: QuestionType = {
    translocoLabel: 'question.qcm',
    translocoLongLabel: 'question-long-label.qcm',
    htmlLabel: 'QCM',
};

export const QRL: QuestionType = {
    translocoLabel: 'question.qrl',
    translocoLongLabel: 'question-long-label.qrl',
    htmlLabel: 'QRL',
};

export const QRE: QuestionType = {
    translocoLabel: 'question.qre',
    translocoLongLabel: 'question-long-label.qre',
    htmlLabel: 'QRE',
};
