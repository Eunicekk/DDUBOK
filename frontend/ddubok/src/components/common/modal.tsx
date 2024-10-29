"use client";

import { useContext } from "react";
import { ModalContext } from "@context/modal-context";

const Modal = () => {
	const { closeModal } = useContext(ModalContext);

	return (
		<div id="modal">
			<div
				id="overlay"
				className="fixed bottom-0 w-screen max-w-[480px] z-10 h-full bg-black bg-opacity-30 backdrop-blur-sm"
				onClick={closeModal}
			></div>
			<div
				id="content"
				className="fixed left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[calc(100%-64px)] bg-white rounded-lg p-8 z-10 font-nexonRegular"
			>
				모달🍀
			</div>
		</div>
	);
};

export default Modal;
