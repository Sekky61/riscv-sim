export type ConfirmModalProps = {
  title: string;
  message: string;
  yesText?: string;
  noText?: string;
  onYes: () => void;
  onNo?: () => void;
};

const ConfirmModal = ({
  title,
  message,
  yesText,
  noText,
  onYes,
  onNo,
}: ConfirmModalProps) => {
  return (
    <div className='m-4'>
      <h1 className='text-xl'>{title}</h1>
      <p className='mb-4'>{message}</p>
      <div>
        <button onClick={onYes} className='button mr-4'>
          {yesText || 'Confirm'}
        </button>
        <button onClick={onNo} className='button'>
          {noText || 'Cancel'}
        </button>
      </div>
    </div>
  );
};

export default ConfirmModal;
