/* eslint-disable @typescript-eslint/no-unused-vars */
import { makeStyles } from '@material-ui/core/styles';
import React from 'react';
import Page from './Page';

const useStyles = makeStyles({
    reader: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        margin: '0 auto',
        width: '100%',
    },
});

interface IProps {
    pages: Array<IReaderPage>
    setCurPage: React.Dispatch<React.SetStateAction<number>>
    settings: IReaderSettings
}

export default function VerticalReader(props: IProps) {
    const { pages, settings, setCurPage } = props;

    const classes = useStyles();

    return (
        <div className={classes.reader}>
            {
                pages.map((page) => (
                    <Page
                        key={page.index}
                        index={page.index}
                        src={page.src}
                        setCurPage={setCurPage}
                        settings={settings}
                    />
                ))
            }
        </div>
    );
}