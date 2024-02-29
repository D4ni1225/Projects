import numpy as np
from numpy import mean
from numpy import std
from matplotlib import pyplot as plt
from sklearn.model_selection import KFold
from keras.models import load_model
from keras.datasets import mnist
from keras.utils import to_categorical
from keras.models import Sequential
from keras.layers import Conv2D
from keras.layers import MaxPooling2D
from keras.layers import Dense, Flatten, Dropout
from keras.optimizers import SGD
from keras.callbacks import LearningRateScheduler, EarlyStopping
from keras.layers import BatchNormalization
from matplotlib import pyplot as plt

def load_dataset():
    #load dataset
    (trainX, trainY), (testX, testY) = mnist.load_data()

    # reshape dataset to have a single channel
    trainX.reshape((trainX.shape[0], 28, 28, 1))
    testX.reshape((testX.shape[0], 28, 28, 1))

    # one-hot encoding labels
    trainY = to_categorical(trainY)
    testY = to_categorical(testY)

    return trainX, trainY, testX, testY

def prep_pixels(train, test):
    # convert from integer to float
    train_norm = train.astype('float32')
    test_norm = test.astype('float32')

    # normalize pixels into [0, 1]
    train_norm /= 255.0
    test_norm /= 255.0

    return train_norm, test_norm

def define_model():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', kernel_initializer='he_uniform', input_shape=(28, 28, 1)))
    model.add(BatchNormalization())
    model.add(Conv2D(32, (3, 3), activation='relu', kernel_initializer='he_uniform'))
    model.add(BatchNormalization())
    model.add(MaxPooling2D((2, 2)))
    model.add(Dropout(0.25))

    model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
    model.add(BatchNormalization())
    model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
    model.add(BatchNormalization())
    model.add(MaxPooling2D((2, 2)))
    model.add(Dropout(0.25))

    model.add(Flatten())
    model.add(Dense(512, activation='relu', kernel_initializer='he_uniform'))
    model.add(BatchNormalization())
    model.add(Dropout(0.25))
    model.add(Dense(1024, activation='relu', kernel_initializer='he_uniform'))
    model.add(BatchNormalization())
    model.add(Dropout(0.25))

    model.add(Dense(10, activation='softmax'))

    #compile model
    opt = SGD(learning_rate=0.01, momentum=0.9)
    model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])
    return model

def evaluate_model(dataX, dataY, n_folds=5):
    scores, histories = list(), list()

    # prepare cross validation
    kfold = KFold(n_folds, shuffle=True, random_state=1)
    
    batch_size = 32
    epochs = 10

    # define LearningRateScheduler
    reduce_lr = LearningRateScheduler(lambda x: 1e-3 * 0.9 ** x)

    # enumerate splits
    for train_ix, test_ix in kfold.split(dataX):
        # define model
        model = define_model()

        # select rows for train and test
        trainX, trainY, testX, testY = dataX[train_ix], dataY[train_ix], dataX[test_ix], dataY[test_ix]

        # fit model
        history = model.fit(trainX, trainY, epochs=epochs, batch_size=batch_size, validation_data=(testX, testY), callbacks=reduce_lr)

        # evaluate model
        _, acc = model.evaluate(testX, testY, verbose=0)
        print('> %.3f' % (acc * 100.0))
		# stores scores
        scores.append(acc)
        histories.append(history)
    return scores, histories

# plot diagnostic learning curves
def summarize_diagnostics(histories):
	for i in range(len(histories)):
		# plot loss
		plt.subplot(2, 1, 1)
		plt.title('Cross Entropy Loss')
		plt.plot(histories[i].history['loss'], color='blue', label='train')
		plt.plot(histories[i].history['val_loss'], color='orange', label='test')
		# plot accuracy
		plt.subplot(2, 1, 2)
		plt.title('Classification Accuracy')
		plt.plot(histories[i].history['accuracy'], color='blue', label='train')
		plt.plot(histories[i].history['val_accuracy'], color='orange', label='test')
	plt.show()
     
     # summarize model performance
def summarize_performance(scores):
	# print summary
	print('Accuracy: mean=%.3f std=%.3f, n=%d' % (mean(scores)*100, std(scores)*100, len(scores)))
	# box and whisker plots of results
	plt.boxplot(scores)
	plt.show()
      
      
# run the test harness for evaluating a model
def run_test_harness(choise):
    # load dataset
    trainX, trainY, testX, testY = load_dataset()
    # prepare pixel data
    trainX, testX = prep_pixels(trainX, testX)

    if choise == 1:
        # evaluate model
        scores, histories = evaluate_model(trainX, trainY, 2)
        # learning curves
        summarize_diagnostics(histories)
        # summarize estimated performance
        summarize_performance(scores)
    elif choise == 2:
         # define model
        model = define_model()
        # fit model
        print('Fitting model...')
        model.fit(trainX, trainY, epochs=10, batch_size=32, verbose=0)
        # save model
        model.save('final_model.h5')
        print('Model saved!')
    elif choise == 3:
         # load model
        model = load_model('final_model.h5')

        # evaluate model on test dataset
        _, acc = model.evaluate(testX, testY, verbose=0)
        print('> %.3f' % (acc * 100.0))


# entry point, run the test harness
choise = int(input('Choose the operation: 1-train; 2-save; 3-test '))
run_test_harness(choise)